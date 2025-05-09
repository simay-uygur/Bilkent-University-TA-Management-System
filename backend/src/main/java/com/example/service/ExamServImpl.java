package com.example.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.dto.FailedRowInfo;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.repo.ClassRoomRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.dto.ExamDto;
import com.example.dto.ExamRoomDto;
import com.example.dto.StudentDto;
import com.example.entity.Exams.Exam;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.Student;
import com.example.repo.ExamRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ExamServImpl implements ExamServ{

    private final ExamRepo examRepo;
    private final CourseOfferingServ courseOfferingServ;
    private final ClassRoomRepo classRoomRepo;

    @Override
    public ExamDto getExam(Exam exam) {
        ExamDto dto = new ExamDto();
        List<ExamRoomDto> rooms = new ArrayList<>();
        for (ExamRoom room : exam.getExamRooms()){
            List<StudentDto> studDtos = new ArrayList<>();
            for (Student stud : room.getStudentsList()) { // some other fields may be added here if needed.
                StudentDto s = new StudentDto();
                s.setStudentId(stud.getStudentId());
                s.setStudentName(stud.getStudentName());
                s.setStudentSurname(stud.getStudentSurname());
                s.setAcademicStatus(stud.getAcademicStatus());
                s.setDepartment(stud.getDepartment());
                studDtos.add(s);
            }
            ExamRoomDto room_DTO = new ExamRoomDto(room.getExamRoom().getClassroomId(), studDtos); // hope it works
            rooms.add(room_DTO);
        }
        dto.setDuration(exam.getDuration());
        //dto.setExamRooms(rooms);
        return dto;
    }

    @Override
    public boolean createExam(Exam exam) {
        examRepo.save(exam);
        return examRepo.existsById(exam.getExamId());
    }


    //for uploading exam with exam rooms, time etc
    @Override
    public Map<String, Object> importExamsFromExcel(MultipartFile file) throws IOException {
        List<Exam> toSave = new ArrayList<>();
        List<FailedRowInfo> failed = new ArrayList<>();

        // Formatter for any cell → its displayed text
        DataFormatter formatter = new DataFormatter();
        // For parsing times like "8:30" or "08:30"
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("H:mm");

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    // 1) DATE cell (col 0) can be Excel date or text
                    Cell dateCell = row.getCell(0);
                    LocalDate examDate;
                    if (dateCell.getCellType() == CellType.NUMERIC
                            && DateUtil.isCellDateFormatted(dateCell)) {
                        // numeric‐formatted date → Java Date → LocalDate
                        java.util.Date d = dateCell.getDateCellValue();
                        examDate = d.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    } else {
                        // any other → formatted text
                        String dateStr = formatter.formatCellValue(dateCell).trim();
                        examDate = LocalDate.parse(dateStr);
                    }

                    // 2) START/END times (col 1 & 2) as text
                    String startTimeStr = formatter.formatCellValue(row.getCell(1)).trim();
                    String endTimeStr   = formatter.formatCellValue(row.getCell(2)).trim();
                    LocalTime sTime = LocalTime.parse(startTimeStr, timeFmt);
                    LocalTime eTime = LocalTime.parse(endTimeStr,   timeFmt);

                    Date start = new Date(
                            examDate.getDayOfMonth(),
                            examDate.getMonthValue(),
                            examDate.getYear(),
                            sTime.getHour(),
                            sTime.getMinute()
                    );
                    Date end = new Date(
                            examDate.getDayOfMonth(),
                            examDate.getMonthValue(),
                            examDate.getYear(),
                            eTime.getHour(),
                            eTime.getMinute()
                    );

                    // 3) Course code (col 3)
                    String courseCode = formatter
                            .formatCellValue(row.getCell(3))
                            .trim()
                            .toUpperCase();
                    CourseOffering offering = courseOfferingServ.getCurrentOffering(courseCode);

                    // 4) Exam type/description (col 4)
                    String type = formatter.formatCellValue(row.getCell(4)).trim();

                    // 5) Rooms CSV (col 5)
                    String roomsCsv = formatter.formatCellValue(row.getCell(5));
                    List<String> roomCodes = Arrays.stream(roomsCsv.split(","))
                            .map(String::trim)
                            .filter(rc -> !rc.isEmpty())
                            .collect(Collectors.toList());

                    // 6) Required TAs (col 6)
                    String reqTas = formatter.formatCellValue(row.getCell(6)).trim();
                    int requiredTAs = Integer.parseInt(reqTas);

                    // 7) Optional workload (col 7)
                    String wl = formatter.formatCellValue(row.getCell(7)).trim();
                    Integer workload = wl.isEmpty()
                            ? null
                            : Integer.valueOf(wl);

                    // --- build the Exam ---
                    Exam exam = new Exam();
                    exam.setDuration(new Event(start, end));
                    exam.setDescription(type);
                    exam.setRequiredTAs(requiredTAs);
                    if (workload != null) exam.setWorkload(workload);
                    exam.setCourseOffering(offering);

                    // 8) build ExamRoom list
                    List<ExamRoom> rooms = new ArrayList<>();
                    for (String rc : roomCodes) {
                        ClassRoom cr = classRoomRepo
                                .findClassRoomByClassroomIdEqualsIgnoreCase(rc)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Classroom not found: " + rc
                                ));
                        ExamRoom er = new ExamRoom();
                        er.setExam(exam);
                        er.setExamRoom(cr);
                        rooms.add(er);
                    }
                    exam.setExamRooms(rooms);

                    toSave.add(exam);

                } catch (Exception e) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }
        }

        if (!toSave.isEmpty()) {
            examRepo.saveAll(toSave);
        }

        return Map.of(
                "successCount", toSave.size(),
                "failedCount",  failed.size(),
                "failedRows",   failed
        );
    }
}
