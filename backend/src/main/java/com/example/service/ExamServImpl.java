package com.example.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.example.dto.*;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.repo.ClassRoomRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.entity.Exams.Exam;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.Student;
import com.example.repo.ExamRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ExamServImpl implements ExamServ{

    private final ExamRepo examRepo;
    private final CourseOfferingServ courseOfferingServ;
    private final ClassRoomRepo classRoomRepo;
    private final LogService log;

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
        log.info("Exam creation", "New exam is created.");
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
                    org.apache.poi.ss.usermodel.Cell dateCell = row.getCell(0);
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
        log.info("Bulk Exam Creation", "");
        return Map.of(
                "successCount", toSave.size(),
                "failedCount",  failed.size(),
                "failedRows",   failed
        );
    }

    @Override
    public byte[] exportExamToPdf(String facultyCode, Integer examId) throws IOException {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));

        // (optional) verify faculty
        String actualFaculty = exam.getCourseOffering()
                .getCourse()
                .getDepartment()
                .getFaculty()
                .getCode();
        if (!actualFaculty.equalsIgnoreCase(facultyCode)) {
            throw new IllegalArgumentException(
                    "Exam " + examId + " is not under faculty " + facultyCode
            );
        }

        // Build groupedData: roomCode → List<StudentMiniDto>
        Map<String, List<StudentMiniDto>> groupedData = new LinkedHashMap<>();
        for (ExamRoom er : exam.getExamRooms()) {
            String roomCode = er.getExamRoom().getClassroomId();

            // combine students + TAs
            List<StudentMiniDto> roster = new ArrayList<>();
            er.getStudentsList().forEach(s -> {
                StudentMiniDto dto = new StudentMiniDto();
                dto.setId(s.getStudentId());
                dto.setName(s.getStudentName());
                dto.setSurname(s.getStudentSurname());
                dto.setIsTa(false);
                roster.add(dto);
            });
            er.getTasAsStudentsList().forEach(t -> {
                StudentMiniDto dto = new StudentMiniDto();
                dto.setId(t.getId());
                dto.setName(t.getName());
                dto.setSurname(t.getSurname());
                dto.setIsTa(true);
                roster.add(dto);
            });

            groupedData.put(roomCode, roster);
        }
        log.info("Exam Export", "Exam with id: " +examId+ " is exported in the pdf format.");
        // render PDF via OpenPDF
        return exportStudentsToPdf(groupedData);
    }

    @Override
    public byte[] exportExamToPdfOnlyId(Integer examId) throws IOException {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));



        // Build groupedData: roomCode → List<StudentMiniDto>
        Map<String, List<StudentMiniDto>> groupedData = new LinkedHashMap<>();
        for (ExamRoom er : exam.getExamRooms()) {
            String roomCode = er.getExamRoom().getClassroomId();

            // combine students + TAs
            List<StudentMiniDto> roster = new ArrayList<>();
            er.getStudentsList().forEach(s -> {
                StudentMiniDto dto = new StudentMiniDto();
                dto.setId(s.getStudentId());
                dto.setName(s.getStudentName());
                dto.setSurname(s.getStudentSurname());
                dto.setIsTa(false);
                roster.add(dto);
            });
            er.getTasAsStudentsList().forEach(t -> {
                StudentMiniDto dto = new StudentMiniDto();
                dto.setId(t.getId());
                dto.setName(t.getName());
                dto.setSurname(t.getSurname());
                dto.setIsTa(true);
                roster.add(dto);
            });

            groupedData.put(roomCode, roster);
        }
        log.info("Exam Export", "Exam with id: " +examId+ " is exported in the pdf format.");
        // render PDF via OpenPDF
        return exportStudentsToPdf(groupedData);
    }


    private byte[] exportStudentsToPdf(Map<String, List<StudentMiniDto>> groupedData)
            throws IOException {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            for (Map.Entry<String, List<StudentMiniDto>> entry : groupedData.entrySet()) {
                String room    = entry.getKey();
                List<StudentMiniDto> roster = entry.getValue();

                document.add(new Paragraph(
                        "Exam Room: " + room,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)
                ));
                document.add(Chunk.NEWLINE);

                PdfPTable table = new PdfPTable(4); // ID, Name, Surname, TA?
                table.setWidthPercentage(100);
                table.addCell("ID");
                table.addCell("Name");
                table.addCell("Surname");
                //table.addCell("TA?");

                for (StudentMiniDto s : roster) {
                    table.addCell(String.valueOf(s.getId()));
                    table.addCell(s.getName());
                    table.addCell(s.getSurname());
//                    table.addCell(
//                            Boolean.TRUE.equals(s.getIsTa()) ? "Yes" : "No"
//                    );
                }

                document.add(table);
                document.newPage();
            }
            log.info("Student Export", "Students were exported in the pdf format.");
            document.close();
            return out.toByteArray();
        } catch (com.lowagie.text.DocumentException e) {
            throw new IOException("PDF generation failed", e);
        }
    }

    @Override
    // @Transactional(readOnly = true)
    public List<ExamDto> getExamsByCourseCode(String courseCode) {
        // 1) find the “current” offering for this course
        CourseOffering offering = courseOfferingServ.getCurrentOffering(courseCode);

        // 2) fetch only that offering’s exams
        return examRepo.findByCourseOffering(offering)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ExamDto toDto(Exam exam) {
        ExamDto dto = new ExamDto();
        dto.setExamId(exam.getExamId());
        dto.setCourseCode(exam.getCourseOffering().getCourse().getCourseCode());
        dto.setDuration(exam.getDuration());
        dto.setType(exam.getDescription());
        dto.setWorkload(exam.getWorkload());
        dto.setRequiredTas(exam.getRequiredTAs());
        // room codes
        dto.setExamRooms(
                exam.getExamRooms().stream()
                        .map(er -> er.getExamRoom().getClassroomId())
                        .toList()
        );
        return dto;
    }
}
