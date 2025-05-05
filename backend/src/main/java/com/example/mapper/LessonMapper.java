package com.example.mapper;

import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonMapper {

    public LessonDto toDto(Lesson lesson) {
        if (lesson == null) return null;

        LessonDto dto = new LessonDto();

        dto.setDuration(toEventDto(lesson.getDuration()));

        ClassRoom room = lesson.getLessonRoom();
        if (room != null) {
            dto.setClassroomId(room.getClassroomId());
            dto.setExamCapacity(room.getExamCapacity());
        }

        dto.setLessonType(lesson.getLessonType().name());
        dto.setSectionId(lesson.getSection() != null ? lesson.getSection().getSectionCode() : null);

        return dto;
    }

    public Lesson toEntity(LessonDto dto) {
        if (dto == null) return null;

        Lesson lesson = new Lesson();

        lesson.setDuration(toEvent(dto.getDuration())); // this line now works

        if (dto.getLessonType() != null) {
            lesson.setLessonType(Lesson.LessonType.valueOf(dto.getLessonType()));
        }

        return lesson;
    }

    private Event toEvent(com.example.dto.EventDto dto) {
        if (dto == null) return null;
        return new Event(
                new com.example.entity.General.Date(
                        dto.getStart().getDay(),
                        dto.getStart().getMonth(),
                        dto.getStart().getYear(),
                        dto.getStart().getHour(),
                        dto.getStart().getMinute()
                ),
                new com.example.entity.General.Date(
                        dto.getFinish().getDay(),
                        dto.getFinish().getMonth(),
                        dto.getFinish().getYear(),
                        dto.getFinish().getHour(),
                        dto.getFinish().getMinute()
                )
        );
    }

    private com.example.dto.EventDto toEventDto(Event event) {
        if (event == null) return null;
        return new com.example.dto.EventDto(
                new com.example.dto.DateDto(
                        event.getStart().getDay(),
                        event.getStart().getMonth(),
                        event.getStart().getYear(),
                        event.getStart().getHour(),
                        event.getStart().getMinute()
                ),
                new com.example.dto.DateDto(
                        event.getFinish().getDay(),
                        event.getFinish().getMonth(),
                        event.getFinish().getYear(),
                        event.getFinish().getHour(),
                        event.getFinish().getMinute()
                )
        );
    }
//
//    /**
//     * Entity → DTO
//     */
//    public LessonDto toDto(Lesson lesson) {
//        if (lesson == null) return null;
//
//        LessonDto dto = new LessonDto();
//
//        // Duration
//        dto.setDuration(lesson.getDuration());
//
//        // Room
//        ClassRoom room = lesson.getLessonRoom();
//        if (room != null) {
//            dto.setClassroomId(room.getClassroomId());
//            dto.setExamCapacity(room.getExamCapacity());
//        }
//
//        // Lesson Type
//        dto.setLessonType(lesson.getLessonType().name());
//
//        // Section ID
//        dto.setSectionId(lesson.getSection() != null ? lesson.getSection().getId() : null);
//
//        return dto;
//    }
//
//    /**
//     * DTO → Entity
//     * ClassRoom and Section should be set in the Service.
//     */
//    public Lesson toEntity(LessonDto dto) {
//        if (dto == null) return null;
//
//        Lesson lesson = new Lesson();
//
//        lesson.setDuration(dto.getDuration());
//
//        if (dto.getLessonType() != null) {
//            lesson.setLessonType(Lesson.LessonType.valueOf(dto.getLessonType()));
//        }
//
//        return lesson;
//    }
}