package com.example.service;

import com.example.dto.DateDto;
import com.example.dto.EventDto;
import com.example.dto.FailedRowInfo;
import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Date;
import com.example.entity.General.DayOfWeek;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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
// older one deleted

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
    public List<LessonDto> createLessonDtoList(LessonDto dto) {
        EventDto duration = dto.getDuration();

        java.time.LocalTime startTime = java.time.LocalTime.of(
                duration.getStart().getHour(),
                duration.getStart().getMinute()
        );
        java.time.LocalTime endTime = java.time.LocalTime.of(
                duration.getFinish().getHour(),
                duration.getFinish().getMinute()
        );

        List<Lesson> chunks = new ArrayList<>();

        Section section = sectionRepo.findBySectionCodeIgnoreCase(dto.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found"));
        ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId()).orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
        Lesson.LessonType type = Lesson.LessonType.valueOf(dto.getLessonType());
        DayOfWeek day = DayOfWeek.valueOf(dto.getDay().toUpperCase());

        while (startTime.plusMinutes(50).isBefore(endTime) || startTime.plusMinutes(50).equals(endTime)) {
            java.time.LocalTime chunkEnd = startTime.plusMinutes(50);

            Date start = new Date(null, null, null, startTime.getHour(), startTime.getMinute());
            Date end = new Date(null, null, null, chunkEnd.getHour(), chunkEnd.getMinute());

            Lesson lesson = new Lesson();
            lesson.setDuration(new Event(start, end));
            lesson.setSection(section);
            lesson.setLessonRoom(room);
            lesson.setLessonType(type);
            lesson.setDay(day);

            chunks.add(lesson);
            startTime = chunkEnd.plusMinutes(10); // add 10-minute break
        }

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("No valid 50-minute lesson chunks could be created");
        }

        lessonRepo.saveAll(chunks);
        return chunks.stream().map(this::convertToDto).collect(Collectors.toList());
    }


    @Override
    public Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException {
        List<Lesson> saved = new ArrayList<>();
        List<FailedRowInfo> failed = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String sectionCode = row.getCell(0).getStringCellValue().trim();
                    String dayStr = row.getCell(1).getStringCellValue().trim().toUpperCase();
                    String startTimeStr = row.getCell(2).getStringCellValue().trim();
                    String endTimeStr = row.getCell(3).getStringCellValue().trim();
                    String typeStr = row.getCell(4).getStringCellValue().trim().toUpperCase();
                    String roomCode = row.getCell(5).getStringCellValue().trim();

                    Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                            .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));
                    ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(roomCode).orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + roomCode));
                    DayOfWeek day = DayOfWeek.valueOf(dayStr);
                    Lesson.LessonType type = Lesson.LessonType.valueOf(typeStr);

                    String[] startTokens = startTimeStr.split(":");
                    String[] endTokens = endTimeStr.split(":");
                    LocalTime startTime = LocalTime.of(Integer.parseInt(startTokens[0]), Integer.parseInt(startTokens[1]));
                    LocalTime endTime = LocalTime.of(Integer.parseInt(endTokens[0]), Integer.parseInt(endTokens[1]));

                    while (!startTime.plusMinutes(50).isAfter(endTime)) {
                        LocalTime chunkEnd = startTime.plusMinutes(50);

                        Date start = new Date(null, null, null, startTime.getHour(), startTime.getMinute());
                        Date finish = new Date(null, null, null, chunkEnd.getHour(), chunkEnd.getMinute());

                        Lesson lesson = new Lesson();
                        lesson.setSection(section);
                        lesson.setLessonRoom(room);
                        lesson.setLessonType(type);
                        lesson.setDay(day);
                        lesson.setDuration(new Event(start, finish));

                        saved.add(lesson);
                        startTime = chunkEnd.plusMinutes(10); // add 10-minute break
                    }

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
/*
    @Override
    public Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException {
        List<Lesson> saved = new ArrayList<>();
        List<FailedRowInfo> failed = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                try {
                    String sectionCode = row.getCell(0).getStringCellValue().trim();
                    String dayStr = row.getCell(1).getStringCellValue().trim().toUpperCase();
                    String startTimeStr = row.getCell(2).getStringCellValue().trim();
                    String endTimeStr = row.getCell(3).getStringCellValue().trim();
                    String typeStr = row.getCell(4).getStringCellValue().trim().toUpperCase();
                    String roomCode = row.getCell(5).getStringCellValue().trim();

                    Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                            .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));
                    ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(roomCode).orElse(null);
                    DayOfWeek day = DayOfWeek.valueOf(dayStr);
                    Lesson.LessonType type = Lesson.LessonType.valueOf(typeStr);

                    String[] startTokens = startTimeStr.split(":");
                    String[] endTokens = endTimeStr.split(":");
                    LocalTime startTime = LocalTime.of(Integer.parseInt(startTokens[0]), Integer.parseInt(startTokens[1]));
                    LocalTime endTime = LocalTime.of(Integer.parseInt(endTokens[0]), Integer.parseInt(endTokens[1]));

                    LocalDate baseDate = LocalDate.of(2025, 5, 1);
                    LocalDate lessonDate = baseDate.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.valueOf(dayStr)));
                    int lessonDay = lessonDate.getDayOfMonth();

                    while (!startTime.plusMinutes(50).isAfter(endTime)) {
                        LocalTime chunkEnd = startTime.plusMinutes(50);
                        Date start = new Date(lessonDay, 5, 2025, startTime.getHour(), startTime.getMinute());
                        Date finish = new Date(lessonDay, 5, 2025, chunkEnd.getHour(), chunkEnd.getMinute());

                        Lesson lesson = new Lesson();
                        lesson.setSection(section);
                        lesson.setLessonRoom(room);
                        lesson.setLessonType(type);
                        lesson.setDay(day);
                        lesson.setDuration(new Event(start, finish));

                        saved.add(lesson);
                        startTime = chunkEnd.plusMinutes(10);
                    }

                } catch (Exception e) {
                    failed.add(new FailedRowInfo(row.getRowNum(), e.getClass().getSimpleName() + ": " + e.getMessage()));
                }
            }
        }

        if (!saved.isEmpty()) lessonRepo.saveAll(saved);

        return Map.of(
                "successCount", saved.size(),
                "failedCount", failed.size(),
                "failedRows", failed
        );
    }*/

//    @Override
//    public Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException {
//        List<Lesson> saved = new ArrayList<>();
//        List<FailedRowInfo> failed = new ArrayList<>();
//
//        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
//            Sheet sheet = wb.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue;
//
//                try {
//                    String sectionCode = row.getCell(0).getStringCellValue().trim();
//                    String dayStr = row.getCell(1).getStringCellValue().trim().toUpperCase();
//                    String startTimeStr = row.getCell(2).getStringCellValue().trim();
//                    String endTimeStr = row.getCell(3).getStringCellValue().trim();
//                    String typeStr = row.getCell(4).getStringCellValue().trim().toUpperCase();
//                    String roomCode = row.getCell(5).getStringCellValue().trim();
//
//                    Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
//                            .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));
//                    ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(roomCode).orElse(null);
//                    DayOfWeek day = DayOfWeek.valueOf(dayStr);
//                    Lesson.LessonType type = Lesson.LessonType.valueOf(typeStr);
//
//                    String[] startTokens = startTimeStr.split(":");
//                    String[] endTokens = endTimeStr.split(":");
//                    java.time.LocalTime startTime = java.time.LocalTime.of(Integer.parseInt(startTokens[0]), Integer.parseInt(startTokens[1]));
//                    java.time.LocalTime endTime = java.time.LocalTime.of(Integer.parseInt(endTokens[0]), Integer.parseInt(endTokens[1]));
//
//                    while (startTime.plusMinutes(50).isBefore(endTime) || startTime.plusMinutes(50).equals(endTime)) {
//                        java.time.LocalTime chunkEnd = startTime.plusMinutes(50);
//
//                        Date start = new Date(null, null, null, startTime.getHour(), startTime.getMinute());
//                        Date end = new Date(null, null, null, chunkEnd.getHour(), chunkEnd.getMinute());
//
//                        Lesson lesson = new Lesson();
//                        lesson.setSection(section);
//                        lesson.setLessonRoom(room);
//                        lesson.setLessonType(type);
//                        lesson.setDay(day);
//                        lesson.setDuration(new Event(start, end));
//
//                        saved.add(lesson);
//                        startTime = chunkEnd.plusMinutes(10);
//                    }
//
//                } catch (Exception e) {
//                    failed.add(new FailedRowInfo(row.getRowNum(), e.getClass().getSimpleName() + ": " + e.getMessage()));
//                }
//            }
//        }
//
//        if (!saved.isEmpty()) {
//            lessonRepo.saveAll(saved);
//        }
//
//        return Map.of(
//                "successCount", saved.size(),
//                "failedCount", failed.size(),
//                "failedRows", failed
//        );
//    }

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

//    @Override
//    public LessonDto createLesson(LessonDto dto) {
//        EventDto duration = dto.getDuration();
//
//        java.time.LocalTime startTime = java.time.LocalTime.of(
//                duration.getStart().getHour(),
//                duration.getStart().getMinute()
//        );
//        java.time.LocalTime endTime = java.time.LocalTime.of(
//                duration.getFinish().getHour(),
//                duration.getFinish().getMinute()
//        );
//
//        Section section = sectionRepo.findBySectionCodeIgnoreCase(dto.getSectionId())
//                .orElseThrow(() -> new IllegalArgumentException("Section not found"));
//        ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId())
//                .orElse(null);
//        Lesson.LessonType type = Lesson.LessonType.valueOf(dto.getLessonType());
//        DayOfWeek day = DayOfWeek.valueOf(dto.getDay().toUpperCase());
//
//        // Create and store only the first 50-minute lesson
//        if (startTime.plusMinutes(50).isAfter(endTime)) {
//            throw new IllegalArgumentException("Duration too short for a 50-minute lesson chunk");
//        }
//
//        java.time.LocalTime chunkEnd = startTime.plusMinutes(50);
//        Date start = new Date(null, null, null, startTime.getHour(), startTime.getMinute());
//        Date end = new Date(null, null, null, chunkEnd.getHour(), chunkEnd.getMinute());
//
//        Lesson lesson = new Lesson();
//        lesson.setDuration(new Event(start, end));
//        lesson.setSection(section);
//        lesson.setLessonRoom(room);
//        lesson.setLessonType(type);
//        lesson.setDay(day);
//
//        Lesson saved = lessonRepo.save(lesson);
//        return convertToDto(saved);
//    }

//    @Override
//    public List<LessonDto> createLessonDtoList(LessonDto dto) {
//        EventDto duration = dto.getDuration();
//
//        java.time.LocalTime startTime = java.time.LocalTime.of(
//                duration.getStart().getHour(),
//                duration.getStart().getMinute()
//        );
//        java.time.LocalTime endTime = java.time.LocalTime.of(
//                duration.getFinish().getHour(),
//                duration.getFinish().getMinute()
//        );
//
//        List<Lesson> chunks = new ArrayList<>();
//
//        Section section = sectionRepo.findBySectionCodeIgnoreCase(dto.getSectionId())
//                .orElseThrow(() -> new IllegalArgumentException("Section not found"));
//        ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId())
//                .orElse(null);
//        Lesson.LessonType type = Lesson.LessonType.valueOf(dto.getLessonType());
//        DayOfWeek day = DayOfWeek.valueOf(dto.getDay().toUpperCase());
//
//        while (startTime.plusMinutes(50).isBefore(endTime) || startTime.plusMinutes(50).equals(endTime)) {
//            java.time.LocalTime chunkEnd = startTime.plusMinutes(50);
//
//            Date start = new Date(null, null, null, startTime.getHour(), startTime.getMinute());
//            Date end = new Date(null, null, null, chunkEnd.getHour(), chunkEnd.getMinute());
//
//            Lesson lesson = new Lesson();
//            lesson.setDuration(new Event(start, end));
//            lesson.setSection(section);
//            lesson.setLessonRoom(room);
//            lesson.setLessonType(type);
//            lesson.setDay(day);
//
//            chunks.add(lesson);
//            startTime = chunkEnd.plusMinutes(10);
//        }
//
//        if (!chunks.isEmpty()) {
//            lessonRepo.saveAll(chunks);
//            return chunks.stream().map(this::convertToDto).collect(Collectors.toList());
//        } else {
//            throw new IllegalArgumentException("No valid 50-minute lesson chunks could be created");
//        }
//    }
//
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
        dto.setDuration(toEventDto(lesson.getDuration()));
        dto.setClassroomId(lesson.getLessonRoom() != null ? lesson.getLessonRoom().getClassroomId() : null);
        dto.setLessonType(lesson.getLessonType().name());
        dto.setSectionId(lesson.getSection().getSectionCode());
        dto.setDay(lesson.getDay().name()); // 🔥 Add this line to distinguish lessons by day
        return dto;
    }

//    private LessonDto convertToDto(Lesson lesson) {
//        LessonDto dto = new LessonDto();
//        dto.setDuration(toEventDto(lesson.getDuration())); // fix here
//        dto.setClassroomId(lesson.getLessonRoom() != null ? lesson.getLessonRoom().getClassroomId() : null);
//        dto.setLessonType(lesson.getLessonType().name());
//        dto.setSectionId(lesson.getSection().getSectionCode());
//        return dto;
//    }

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
create returning list dto
@Override
public List<LessonDto> createLesson(LessonDto dto) {
    EventDto duration = dto.getDuration();

    java.time.LocalTime startTime = java.time.LocalTime.of(
            duration.getStart().getHour(),
            duration.getStart().getMinute()
    );
    java.time.LocalTime endTime = java.time.LocalTime.of(
            duration.getFinish().getHour(),
            duration.getFinish().getMinute()
    );

    int day = duration.getStart().getDay();
    int month = duration.getStart().getMonth();
    int year = duration.getStart().getYear();

    List<Lesson> chunks = new ArrayList<>();

    Section section = sectionRepo.findBySectionCodeIgnoreCase(dto.getSectionId())
            .orElseThrow(() -> new IllegalArgumentException("Section not found: " + dto.getSectionId()));
    ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId())
            .orElse(null);
    Lesson.LessonType type = Lesson.LessonType.valueOf(dto.getLessonType());

    while (startTime.plusMinutes(50).isBefore(endTime) || startTime.plusMinutes(50).equals(endTime)) {
        java.time.LocalTime chunkEnd = startTime.plusMinutes(50);

        Date start = new Date(day, month, year, startTime.getHour(), startTime.getMinute());
        Date end = new Date(day, month, year, chunkEnd.getHour(), chunkEnd.getMinute());

        Lesson lesson = new Lesson();
        lesson.setDuration(new Event(start, end));
        lesson.setSection(section);
        lesson.setLessonRoom(room);
        lesson.setLessonType(type);

        chunks.add(lesson);
        startTime = chunkEnd.plusMinutes(10);
    }

    if (!chunks.isEmpty()) {
        lessonRepo.saveAll(chunks);
        return chunks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    } else {
        throw new IllegalArgumentException("No valid 50-minute lesson chunks could be created");
    }
}
 */
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






// oldest ones
//    @Override
//    public Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException {
//        List<Lesson> saved = new ArrayList<>();
//        List<FailedRowInfo> failed = new ArrayList<>();
//
//        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
//            Sheet sheet = wb.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue;
//
//                try {
//                    String sectionCode = row.getCell(0).getStringCellValue().trim(); // CS-102-1
//                    String dayStr = row.getCell(1).getStringCellValue().trim().toUpperCase(); // MONDAY
//                    String startTimeStr = row.getCell(2).getStringCellValue().trim(); // 08:30
//                    String endTimeStr = row.getCell(3).getStringCellValue().trim();   // 10:20
//                    String typeStr = row.getCell(4).getStringCellValue().trim().toUpperCase(); // LESSON / SPARE_HOUR
//                    String roomCode = row.getCell(5).getStringCellValue().trim();
//
//                    Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
//                            .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));
//
//                    String[] startTokens = startTimeStr.split(":");
//                    String[] endTokens = endTimeStr.split(":");
//
//                    int startHour = Integer.parseInt(startTokens[0]);
//                    int startMinute = Integer.parseInt(startTokens[1]);
//                    int endHour = Integer.parseInt(endTokens[0]);
//                    int endMinute = Integer.parseInt(endTokens[1]);
//
//                    java.time.DayOfWeek targetDay = java.time.DayOfWeek.valueOf(dayStr);
//                    java.time.LocalDate baseDate = java.time.LocalDate.of(2025, 5, 1);
//                    java.time.LocalDate lessonDate = baseDate.with(java.time.temporal.TemporalAdjusters.nextOrSame(targetDay));
//                    int lessonDay = lessonDate.getDayOfMonth();
//
//                    com.example.entity.General.Date start = new com.example.entity.General.Date(lessonDay, 5, 2025, startHour, startMinute);
//                    com.example.entity.General.Date end = new com.example.entity.General.Date(lessonDay, 5, 2025, endHour, endMinute);
//
//                    Event event = new Event(start, end);
//
//                    Lesson lesson = new Lesson();
//                    lesson.setSection(section);
//                    lesson.setDuration(event);
//                    lesson.setLessonType(Lesson.LessonType.valueOf(typeStr));
//                    lesson.setLessonRoom(
//                            classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(roomCode).orElse(null)
//                    );
//
//                    saved.add(lesson);
//
//                } catch (Exception e) {
//                    failed.add(new FailedRowInfo(
//                            row.getRowNum(),
//                            e.getClass().getSimpleName() + ": " + e.getMessage()
//                    ));
//                }
//            }
//        }
//
//        if (!saved.isEmpty()) {
//            lessonRepo.saveAll(saved);
//        }
//
//        return Map.of(
//                "successCount", saved.size(),
//                "failedCount", failed.size(),
//                "failedRows", failed
//        );
//    }


//    @Override
//    public LessonDto createLesson(LessonDto dto) {
//        EventDto duration = dto.getDuration();
//
//        // Extract times
//        java.time.LocalTime startTime = java.time.LocalTime.of(
//                duration.getStart().getHour(),
//                duration.getStart().getMinute()
//        );
//        java.time.LocalTime endTime = java.time.LocalTime.of(
//                duration.getFinish().getHour(),
//                duration.getFinish().getMinute()
//        );
//
//        int day = duration.getStart().getDay();
//        int month = duration.getStart().getMonth();
//        int year = duration.getStart().getYear();
//
//        List<Lesson> chunks = new ArrayList<>();
//
//        // Shared info
//        Section section = sectionRepo.findBySectionCodeIgnoreCase(dto.getSectionId())
//                .orElseThrow(() -> new IllegalArgumentException("Section not found"));
//        ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId())
//                .orElse(null);
//        Lesson.LessonType type = Lesson.LessonType.valueOf(dto.getLessonType());
//
//        // Create 50-min blocks
//        while (startTime.plusMinutes(50).isBefore(endTime) || startTime.plusMinutes(50).equals(endTime)) {
//            java.time.LocalTime chunkEnd = startTime.plusMinutes(50);
//
//            Date start = new Date(day, month, year, startTime.getHour(), startTime.getMinute());
//            Date end = new Date(day, month, year, chunkEnd.getHour(), chunkEnd.getMinute());
//
//            Lesson lesson = new Lesson();
//            lesson.setDuration(new Event(start, end));
//            lesson.setSection(section);
//            lesson.setLessonRoom(room);
//            lesson.setLessonType(type);
//
//            chunks.add(lesson);
//
//            startTime = chunkEnd.plusMinutes(10); // skip 10-min break
//        }
//
//        // Save all and return first as representative
//        if (!chunks.isEmpty()) {
//            lessonRepo.saveAll(chunks);
//            return convertToDto(chunks.get(0)); // or return a list if preferred
//        } else {
//            throw new IllegalArgumentException("No valid 50-minute chunks could be created");
//        }
//    }