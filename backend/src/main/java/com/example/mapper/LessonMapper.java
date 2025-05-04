package com.example.mapper;

import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.General.ClassRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class LessonMapper {

    /**
     * Entity → DTO
     */
    public LessonDto toDto(Lesson lesson) {
        if (lesson == null) return null;

        LessonDto dto = new LessonDto();

        // ISO-8601 duration string (e.g. "PT1H30M")
        dto.setDuration(lesson.getDuration() != null
                ? lesson.getDuration().toString()
                : null
        );

        // classRoom → use classroomId and examCapacity
        ClassRoom room = lesson.getLessonRoom();
        if (room != null) {
            dto.setRoom(room.getClassroomId());
            dto.setExamCapacity(room.getExamCapacity());
        }

        return dto;
    }

    /**
     * DTO → Entity
     *
     * Note: we only parse the duration here.
     *       Assigning the real ClassRoom entity is done in the service.
     */
    public Lesson toEntity(LessonDto dto) {
        if (dto == null) return null;

        Lesson lesson = new Lesson();

//        if (dto.getDuration() != null && !dto.getDuration().isEmpty()) {
//            lesson.setDuration(Duration.parse(dto.getDuration()));
//        }

        // lesson.setLessonRoom(...) must be assigned in the service
        return lesson;
    }
}