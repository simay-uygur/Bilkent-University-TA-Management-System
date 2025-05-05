package com.example.service;

import com.example.dto.DateDto;
import com.example.dto.EventDto;
import com.example.dto.FailedRowInfo;
import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.General.Date;
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
