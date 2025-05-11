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
    private final LogService log;
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
/*

// … inside your per-row loop, after you’ve parsed section, room and day …

// existing lessons for this section/day
List<Lesson> existingSection = lessonRepo
    .findBySection_SectionCodeIgnoreCaseAndDay(sectionCode, day);

// existing lessons for this room/day
List<Lesson> existingRoom = lessonRepo
    .findByLessonRoom_ClassroomIdEqualsIgnoreCaseAndDay(roomCode, day);

// combine them so you catch *any* overlap
List<Lesson> existing = new ArrayList<>();
existing.addAll(existingSection);
existing.addAll(existingRoom);

// … now your slot‐by‐slot loop …
while (!cursor.plusMinutes(50).isAfter(endTime)) {
    LocalTime slotStart = cursor;
    LocalTime slotEnd   = cursor.plusMinutes(50);

    // conflict if overlaps *any* existingSection *or* existingRoom lesson
    boolean conflict = existing.stream().anyMatch(l -> {
        LocalTime exStart = LocalTime.of(
            l.getDuration().getStart().getHour(),
            l.getDuration().getStart().getMinute());
        LocalTime exEnd = LocalTime.of(
            l.getDuration().getFinish().getHour(),
            l.getDuration().getFinish().getMinute());
        return slotStart.isBefore(exEnd) && exStart.isBefore(slotEnd);
    });
    if (conflict) {
        failed.add(new FailedRowInfo(
            row.getRowNum(),
            "Time conflict (section or room) on " + slotStart + "–" + slotEnd
        ));
        break;  // skip this entire row
    }

    // … build and buffer the chunk …
    cursor = slotEnd.plusMinutes(10);
}

// on success you add your chunks to toSave and also do:
// existing.addAll(rowChunks);
// so later slots in the same import see the newly scheduled lessons too.
 */
    @Override
    public List<LessonDto> createLessonDtoList(LessonDto dto) {

        EventDto duration = dto.getDuration();
        LocalTime startTime = LocalTime.of(duration.getStart().getHour(),
                duration.getStart().getMinute());
        LocalTime endTime   = LocalTime.of(duration.getFinish().getHour(),
                duration.getFinish().getMinute());

        Section section = sectionRepo.findBySectionCodeIgnoreCase(dto.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found"));
        ClassRoom room = classRoomRepo.findClassRoomByClassroomIdEqualsIgnoreCase(dto.getClassroomId())
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
        Lesson.LessonType type = Lesson.LessonType.valueOf(dto.getLessonType());
        DayOfWeek day = DayOfWeek.valueOf(dto.getDay().toUpperCase());

        // --- load existing lessons for this section/day and this room/day ---
        List<Lesson> existingSection = lessonRepo
                .findBySection_SectionCodeIgnoreCaseAndDay(section.getSectionCode(), day);
        List<Lesson> existingRoom = lessonRepo
                .findByLessonRoom_ClassroomIdEqualsIgnoreCaseAndDay(room.getClassroomId(), day);

        List<Lesson> existing = new ArrayList<>(existingSection);
        existing.addAll(existingRoom);

        List<Lesson> chunks = new ArrayList<>();

        // --- iterate over 50-minute + 10-minute slots ---
        while (!startTime.plusMinutes(50).isAfter(endTime)) {
            LocalTime slotStart = startTime;
            LocalTime slotEnd   = startTime.plusMinutes(50);

            // 1) conflict if overlapping any existing lesson (section OR room)
            boolean conflict = existing.stream().anyMatch(l -> {
                LocalTime exStart = LocalTime.of(
                        l.getDuration().getStart().getHour(),
                        l.getDuration().getStart().getMinute());
                LocalTime exEnd = LocalTime.of(
                        l.getDuration().getFinish().getHour(),
                        l.getDuration().getFinish().getMinute());
                // overlap <=> slotStart < exEnd && exStart < slotEnd
                return slotStart.isBefore(exEnd) && exStart.isBefore(slotEnd);
            });

            if (conflict) {
                throw new IllegalArgumentException(
                        String.format("Time conflict for section %s or room %s on %s %s–%s",
                                section.getSectionCode(),
                                room.getClassroomId(),
                                day, slotStart, slotEnd)
                );
            }

            // 2) no conflict → build the lesson chunk
            Date start = new Date(null, null, null,
                    slotStart.getHour(), slotStart.getMinute());
            Date finish = new Date(null, null, null,
                    slotEnd.getHour(),    slotEnd.getMinute());
            Lesson lesson = new Lesson();
            lesson.setSection(section);
            lesson.setLessonRoom(room);
            lesson.setLessonType(type);
            lesson.setDay(day);
            lesson.setDuration(new Event(start, finish));

            // buffer it
            chunks.add(lesson);
            existing.add(lesson);       // so next slots see this new one

            // advance to next slot (+10min break)
            startTime = slotEnd.plusMinutes(10);
        }

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("No valid 50-minute lesson chunks could be created");
        }
        // 3) persist and return DTOs
        lessonRepo.saveAll(chunks);
        return chunks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException {
        List<Lesson> toSave = new ArrayList<>();
        List<FailedRowInfo> failed = new ArrayList<>();

        // ← Add this
        DataFormatter formatter = new DataFormatter();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // header

                try {
                    // --- parse row ---
                    String sectionCode  = row.getCell(0).getStringCellValue().trim();
                    String dayStr       = row.getCell(1).getStringCellValue().trim().toUpperCase();
                    // ← use formatter here instead of getStringCellValue()
                    String startTimeStr = formatter.formatCellValue(row.getCell(2)).trim();
                    String endTimeStr   = formatter.formatCellValue(row.getCell(3)).trim();
                    String typeStr      = row.getCell(4).getStringCellValue().trim().toUpperCase();
                    String roomCode     = row.getCell(5).getStringCellValue().trim();

                    Section section = sectionRepo
                            .findBySectionCodeIgnoreCase(sectionCode)
                            .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));
                    ClassRoom room = classRoomRepo
                            .findClassRoomByClassroomIdEqualsIgnoreCase(roomCode)
                            .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + roomCode));

                    DayOfWeek day = DayOfWeek.valueOf(dayStr);
                    Lesson.LessonType type = Lesson.LessonType.valueOf(typeStr);

                    String[] startTokens = startTimeStr.split(":");
                    String[] endTokens   = endTimeStr.split(":");
                    LocalTime startTime = LocalTime.of(
                            Integer.parseInt(startTokens[0]), Integer.parseInt(startTokens[1]));
                    LocalTime endTime = LocalTime.of(
                            Integer.parseInt(endTokens[0]),   Integer.parseInt(endTokens[1]));

                    // --- load existing lessons ---
                    List<Lesson> existingSection = lessonRepo
                            .findBySection_SectionCodeIgnoreCaseAndDay(sectionCode, day);
                    List<Lesson> existingRoom = lessonRepo
                            .findByLessonRoom_ClassroomIdEqualsIgnoreCaseAndDay(roomCode, day);

                    List<Lesson> existing = new ArrayList<>();
                    existing.addAll(existingSection);
                    existing.addAll(existingRoom);

                    // --- build & check chunks ---
                    List<Lesson> rowChunks = new ArrayList<>();
                    boolean conflict = false;
                    LocalTime cursor = startTime;

                    while (!cursor.plusMinutes(50).isAfter(endTime)) {
                        LocalTime slotStart = cursor;
                        LocalTime slotEnd   = cursor.plusMinutes(50);

                        // conflict?
                        if (existing.stream().anyMatch(l -> {
                            LocalTime exStart = LocalTime.of(
                                    l.getDuration().getStart().getHour(),
                                    l.getDuration().getStart().getMinute());
                            LocalTime exEnd = LocalTime.of(
                                    l.getDuration().getFinish().getHour(),
                                    l.getDuration().getFinish().getMinute());
                            return slotStart.isBefore(exEnd) && exStart.isBefore(slotEnd);
                        })) {
                            failed.add(new FailedRowInfo(
                                    row.getRowNum(),
                                    "Time conflict on " + slotStart + "–" + slotEnd //this error message can be improved
                            ));
                            conflict = true;
                            break;
                        }

                        // no conflict → create chunk
                        Date s = new Date(null, null, null, slotStart.getHour(), slotStart.getMinute());
                        Date f = new Date(null, null, null, slotEnd.getHour(),   slotEnd.getMinute());
                        Lesson lesson = new Lesson();
                        lesson.setSection(section);
                        lesson.setLessonRoom(room);
                        lesson.setLessonType(type);
                        lesson.setDay(day);
                        lesson.setDuration(new Event(s, f));

                        rowChunks.add(lesson);
                        existing.add(lesson);
                        cursor = slotEnd.plusMinutes(10);
                    }

                    if (!conflict) {
                        toSave.addAll(rowChunks);
                    }

                } catch (Exception e) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }
        }

        if (!toSave.isEmpty()) {
            lessonRepo.saveAll(toSave);
        }
        log.info("Bulk Lesson Upload", "");
        return Map.of(
                "successCount", toSave.size(),
                "failedCount",  failed.size(),
                "failedRows",   failed
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
    public void deleteLesson(Long id) {
        if (!lessonRepo.existsById(id)) {
            throw new NoSuchElementException("Lesson not found with id: " + id);
        }
        log.info("Lesson deletion", "Lesson with id: " + id + " is deleted.");
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
        log.info("Lesson update", "Lesson with id: " + id + " is updated");
        return convertToDto(lessonRepo.save(lesson));
    }

    private LessonDto convertToDto(Lesson lesson) {
        LessonDto dto = new LessonDto();
        dto.setDuration(toEventDto(lesson.getDuration()));
        dto.setClassroomId(lesson.getLessonRoom() != null ? lesson.getLessonRoom().getClassroomId() : null);
        dto.setLessonType(lesson.getLessonType().name());
        dto.setSectionId(lesson.getSection().getSectionCode());
        dto.setDay(lesson.getDay().name()); // added line to distinguish lessons by day
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