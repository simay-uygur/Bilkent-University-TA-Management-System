package com.example.service;

import com.example.dto.DateDto;
import com.example.dto.EventDto;
import com.example.dto.FailedRowInfo;
import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.repo.ClassRoomRepo;
import com.example.repo.LessonRepo;
import com.example.repo.SectionRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServImpl implements LessonServ {

    private final LessonRepo lessonRepo;
    private final SectionRepo sectionRepo;
    private final ClassRoomRepo classRoomRepo;
/*     @Override
@Transactional
public Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException {
    List<Lesson> successful = new ArrayList<>();
    List<FailedRowInfo> failed = new ArrayList<>();

    try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = wb.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;  // Skip header
            
            try {
                // 1) Extract data from Excel
                String sectionCode = getCellStringValue(row, 0); // CS-319-1-2025-SPRING
                String dayString = getCellStringValue(row, 1);    // Monday
                String startTime = getCellStringValue(row, 2);    // 10:40
                String endTime = getCellStringValue(row, 3);      // 12:30
                String roomCode = getCellStringValue(row, 4);     // B-201
                
                if (sectionCode == null || dayString == null || startTime == null || 
                    endTime == null || roomCode == null) {
                    throw new IllegalArgumentException("Missing required data in row " + row.getRowNum());
                }
                
                // 2) Find the section
                Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Section not found with code: " + sectionCode));
                
                // 3) Find or create classroom
                ClassRoom room = classRoomRepo.findClassRoomByClassroomId(roomCode)
                    .orElseGet(() -> {
                        ClassRoom newRoom = new ClassRoom();
                        newRoom.setClassroomId(roomCode);
                        newRoom.setClassCapacity(50); // Default values
                        newRoom.setExamCapacity(30);
                        return classRoomRepo.save(newRoom);
                    });
                
                // 4) Parse times and create event
                String[] startParts = startTime.split(":");
                String[] endParts = endTime.split(":");
                
                int startHour = Integer.parseInt(startParts[0]);
                int startMinute = Integer.parseInt(startParts[1]);
                int endHour = Integer.parseInt(endParts[0]);
                int endMinute = Integer.parseInt(endParts[1]);
                
                // Get day index from string (0 = Monday, 6 = Sunday)
                int dayOfWeek = mapDayStringToDayIndex(dayString); 
                
                // Get current date info for the day of week
                Calendar calendar = Calendar.getInstance();
                while (calendar.get(Calendar.DAY_OF_WEEK) != mapDayIndexToCalendarDay(dayOfWeek)) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                
                // Create start Date
                Date startDate = new Date();
                startDate.setYear(calendar.get(Calendar.YEAR));
                startDate.setMonth(calendar.get(Calendar.MONTH) + 1); // Calendar months are 0-based
                startDate.setDay(calendar.get(Calendar.DAY_OF_MONTH));
                startDate.setHour(startHour);
                startDate.setMinute(startMinute);
                
                // Create end Date
                Date endDate = new Date();
                endDate.setYear(calendar.get(Calendar.YEAR));
                endDate.setMonth(calendar.get(Calendar.MONTH) + 1);
                endDate.setDay(calendar.get(Calendar.DAY_OF_MONTH));
                endDate.setHour(endHour);
                endDate.setMinute(endMinute);
                
                // Create Event
                Event event = new Event(startDate, endDate);
                
                // 5) Check for time conflicts
                checkForTimeConflict(event, room);
                
                // 6) Create and save Lesson
                Lesson lesson = new Lesson();
                lesson.setSection(section);
                lesson.setLessonRoom(room);
                lesson.setDuration(event);
                lesson.setLessonType(Lesson.LessonType.LESSON);
                
                Lesson savedLesson = lessonRepo.save(lesson);
                successful.add(savedLesson);
                
            } catch (Exception e) {
                failed.add(new FailedRowInfo(
                    row.getRowNum(), 
                    e.getClass().getSimpleName() + ": " + e.getMessage()
                ));
            }
        }
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("successCount", successful.size());
    result.put("failedCount", failed.size());
    result.put("failedRows", failed);
    return result;
}
private String getCellStringValue(Row row, int cellIndex) {
    Cell cell = row.getCell(cellIndex);
    if (cell == null) {
        return null;
    }
    
    switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue().trim();
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toString();
            }
            return String.valueOf((int)cell.getNumericCellValue());
        default:
            return "";
    }
} */

// Helper method to convert day string to day index (0-6)
/* private int mapDayStringToDayIndex(String dayString) {
    switch (dayString.toLowerCase()) {
        case "monday": return 0;
        case "tuesday": return 1;
        case "wednesday": return 2;
        case "thursday": return 3;
        case "friday": return 4;
        case "saturday": return 5;
        case "sunday": return 6;
        default: throw new IllegalArgumentException("Invalid day: " + dayString);
    }
}

// Helper method to convert day index to Calendar day constant
private int mapDayIndexToCalendarDay(int dayIndex) {
    switch (dayIndex) {
        case 0: return Calendar.MONDAY;
        case 1: return Calendar.TUESDAY;
        case 2: return Calendar.WEDNESDAY;
        case 3: return Calendar.THURSDAY;
        case 4: return Calendar.FRIDAY;
        case 5: return Calendar.SATURDAY;
        case 6: return Calendar.SUNDAY;
        default: throw new IllegalArgumentException("Invalid day index: " + dayIndex);
    }
}

// Check for time conflicts in the same room
private void checkForTimeConflict(Event newEvent, ClassRoom room) {
    List<Lesson> existingLessons = lessonRepo.findByLessonRoom(room);
    
    for (Lesson existingLesson : existingLessons) {
        Event existingEvent = existingLesson.getDuration();
        
        // Use your existing has() method to detect overlap
        if (existingEvent.has(newEvent) || newEvent.has(existingEvent)) {
            throw new IllegalStateException(
                "Time conflict detected in room " + room.getClassroomId() + 
                " between " + newEvent + " and existing lesson " + existingEvent);
        }
    }
} */
    @Override
    public Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException {
        List<Lesson> saved = new ArrayList<>();
        List<FailedRowInfo> failed = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String sectionCode = row.getCell(0).getStringCellValue().trim(); // CS-102-1
                    String dayStr = row.getCell(1).getStringCellValue().trim().toUpperCase(); // MONDAY
                    String startTimeStr = row.getCell(2).getStringCellValue().trim(); // 08:30
                    String endTimeStr = row.getCell(3).getStringCellValue().trim();   // 10:20
                    String typeStr = row.getCell(4).getStringCellValue().trim().toUpperCase(); // LESSON / SPARE_HOUR
                    String roomCode = row.getCell(5).getStringCellValue().trim();

                    Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                            .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));

                    String[] startTokens = startTimeStr.split(":");
                    String[] endTokens = endTimeStr.split(":");

                    int startHour = Integer.parseInt(startTokens[0]);
                    int startMinute = Integer.parseInt(startTokens[1]);
                    int endHour = Integer.parseInt(endTokens[0]);
                    int endMinute = Integer.parseInt(endTokens[1]);

                    java.time.DayOfWeek targetDay = java.time.DayOfWeek.valueOf(dayStr);
                    java.time.LocalDate baseDate = java.time.LocalDate.of(2025, 5, 1);
                    java.time.LocalDate lessonDate = baseDate.with(java.time.temporal.TemporalAdjusters.nextOrSame(targetDay));
                    int lessonDay = lessonDate.getDayOfMonth();

                    com.example.entity.General.Date start = new com.example.entity.General.Date(lessonDay, 5, 2025, startHour, startMinute);
                    com.example.entity.General.Date end = new com.example.entity.General.Date(lessonDay, 5, 2025, endHour, endMinute);

                    Event event = new Event(start, end);

                    Lesson lesson = new Lesson();
                    lesson.setSection(section);
                    lesson.setDuration(event);
                    lesson.setLessonType(Lesson.LessonType.valueOf(typeStr));
                    lesson.setLessonRoom(
                            classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(roomCode).orElse(null)
                    );

                    saved.add(lesson);

                } catch (Exception e) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }
        }

        if (!saved.isEmpty()) {
            lessonRepo.saveAll(saved);
        }

        return Map.of(
                "successCount", saved.size(),
                "failedCount", failed.size(),
                "failedRows", failed
        );
    }

    @Override
    public List<LessonDto> getAllLessons() {
        return lessonRepo.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LessonDto getLessonById(Long id) {
        Lesson lesson = lessonRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found with id: " + id));
        return convertToDto(lesson);
    }

    @Override
    public LessonDto createLesson(LessonDto dto) {
        Lesson lesson = convertToEntity(dto);
        return convertToDto(lessonRepo.save(lesson));
    }

    @Override
    public void deleteLesson(Long id) {
        if (!lessonRepo.existsById(id)) {
            throw new NoSuchElementException("Lesson not found with id: " + id);
        }
        lessonRepo.deleteById(id);
    }

    @Override
    public LessonDto updateLesson(Long id, LessonDto dto) {
        Lesson lesson = lessonRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found with id: " + id));

        lesson.setDuration(toEvent(dto.getDuration())); // fix here
        lesson.setLessonRoom(
                classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId()).orElse(null)
        );
        lesson.setLessonType(Lesson.LessonType.valueOf(dto.getLessonType()));

        return convertToDto(lessonRepo.save(lesson));
    }

    private LessonDto convertToDto(Lesson lesson) {
        LessonDto dto = new LessonDto();
        dto.setDuration(toEventDto(lesson.getDuration())); // fix here
        dto.setClassroomId(lesson.getLessonRoom() != null ? lesson.getLessonRoom().getClassroomId() : null);
        dto.setLessonType(lesson.getLessonType().name());
        dto.setSectionId(lesson.getSection().getSectionCode());
        return dto;
    }

    private Lesson convertToEntity(LessonDto dto) {
        Lesson lesson = new Lesson();
        lesson.setDuration(toEvent(dto.getDuration())); // fix here
        lesson.setLessonRoom(
                classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId()).orElse(null)
        );
        lesson.setSection(
                sectionRepo.findBySectionCodeIgnoreCase(dto.getSectionId())
                        .orElseThrow(() -> new IllegalArgumentException("Section not found"))
        );
        lesson.setLessonType(Lesson.LessonType.valueOf(dto.getLessonType()));
        return lesson;
    }

    private Event toEvent(EventDto dto) {
        return new Event(
                new Date(dto.getStart().getDay(), dto.getStart().getMonth(), dto.getStart().getYear(), dto.getStart().getHour(), dto.getStart().getMinute()),
                new Date(dto.getFinish().getDay(), dto.getFinish().getMonth(), dto.getFinish().getYear(), dto.getFinish().getHour(), dto.getFinish().getMinute())
        );
    }

    private EventDto toEventDto(Event event) {
        return new EventDto(
                new DateDto(event.getStart().getDay(), event.getStart().getMonth(), event.getStart().getYear(), event.getStart().getHour(), event.getStart().getMinute()),
                new DateDto(event.getFinish().getDay(), event.getFinish().getMonth(), event.getFinish().getYear(), event.getFinish().getHour(), event.getFinish().getMinute())
        );
    }
}

/*

package com.example.service;

import com.example.dto.FailedRowInfo;
import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Event;
import com.example.repo.ClassRoomRepo;
import com.example.repo.LessonRepo;
import com.example.repo.SectionRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServImpl implements LessonServ {

    private final LessonRepo lessonRepo;
    private final SectionRepo sectionRepo;
    private final ClassRoomRepo classRoomRepo;

    @Override
    public Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException {
        List<Lesson> saved = new ArrayList<>();
        List<FailedRowInfo> failed = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String sectionCode = row.getCell(0).getStringCellValue().trim(); // CS-102-1
                    String dayStr = row.getCell(1).getStringCellValue().trim().toUpperCase(); // MONDAY
                    String startTimeStr = row.getCell(2).getStringCellValue().trim(); // 08:30
                    String endTimeStr = row.getCell(3).getStringCellValue().trim();   // 10:20
                    String typeStr = row.getCell(4).getStringCellValue().trim().toUpperCase(); // LESSON / SPARE_HOUR
                    String roomCode = row.getCell(5).getStringCellValue().trim();

                    Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                            .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));

                    String[] startTokens = startTimeStr.split(":");
                    String[] endTokens = endTimeStr.split(":");

                    int startHour = Integer.parseInt(startTokens[0]);
                    int startMinute = Integer.parseInt(startTokens[1]);
                    int endHour = Integer.parseInt(endTokens[0]);
                    int endMinute = Integer.parseInt(endTokens[1]);

                    java.time.DayOfWeek targetDay = java.time.DayOfWeek.valueOf(dayStr);
                    java.time.LocalDate baseDate = java.time.LocalDate.of(2025, 5, 1);
                    java.time.LocalDate lessonDate = baseDate.with(java.time.temporal.TemporalAdjusters.nextOrSame(targetDay));
                    int lessonDay = lessonDate.getDayOfMonth();

                    com.example.entity.General.Date start = new com.example.entity.General.Date(lessonDay, 5, 2025, startHour, startMinute);
                    com.example.entity.General.Date end = new com.example.entity.General.Date(lessonDay, 5, 2025, endHour, endMinute);

                    Event event = new Event(start, end);

                    Lesson lesson = new Lesson();
                    lesson.setSection(section);
                    lesson.setDuration(event);
                    lesson.setLessonType(Lesson.LessonType.valueOf(typeStr));
                    lesson.setLessonRoom(
                            classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(roomCode).orElse(null)
                    );

                    saved.add(lesson);

                } catch (Exception e) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }
        }

        if (!saved.isEmpty()) {
            lessonRepo.saveAll(saved);
        }

        return Map.of(
                "successCount", saved.size(),
                "failedCount", failed.size(),
                "failedRows", failed
        );
    }

    @Override
    public List<LessonDto> getAllLessons() {
        return lessonRepo.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LessonDto getLessonById(Long id) {
        Lesson lesson = lessonRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found with id: " + id));
        return convertToDto(lesson);
    }

    @Override
    public LessonDto createLesson(LessonDto dto) {
        Lesson lesson = convertToEntity(dto);
        return convertToDto(lessonRepo.save(lesson));
    }

    @Override
    public LessonDto updateLesson(Long id, LessonDto dto) {
        Lesson lesson = lessonRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found with id: " + id));

        lesson.setDuration(dto.getDuration());
        lesson.setLessonRoom(classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId()).orElse(null));
        lesson.setLessonType(Lesson.LessonType.valueOf(dto.getLessonType()));

        return convertToDto(lessonRepo.save(lesson));
    }

    @Override
    public void deleteLesson(Long id) {
        if (!lessonRepo.existsById(id)) {
            throw new NoSuchElementException("Lesson not found with id: " + id);
        }
        lessonRepo.deleteById(id);
    }

    private LessonDto convertToDto(Lesson lesson) {
        LessonDto dto = new LessonDto();
        dto.setDuration(lesson.getDuration());
        dto.setClassroomId(lesson.getLessonRoom() != null ? lesson.getLessonRoom().getClassroomId() : null);
        dto.setLessonType(lesson.getLessonType().name());
        dto.setSectionId(lesson.getSection().getId());
        return dto;
    }

    private Lesson convertToEntity(LessonDto dto) {
        Lesson lesson = new Lesson();
        lesson.setDuration(dto.getDuration());
        lesson.setLessonRoom(
                classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId()).orElse(null)
        );
        lesson.setSection(
                sectionRepo.findById(dto.getSectionId()).orElseThrow(() -> new IllegalArgumentException("Section not found"))
        );
        lesson.setLessonType(Lesson.LessonType.valueOf(dto.getLessonType()));
        return lesson;
    }
}

 */
