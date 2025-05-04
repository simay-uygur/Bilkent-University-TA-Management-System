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
        // ISO-8601 duration string
        dto.setDuration(lesson.getDuration() != null
                ? lesson.getDuration().toString()
                : null
        );

        // classRoom → classCode + examRoom
        ClassRoom room = lesson.getLessonRoom();
        if (room != null) {
            dto.setClassCode(room.getClassroomId());
            dto.setRoom(
                    room.getExamRoom() != null
                            ? room.getExamRoom().toString()
                            : null
            );
        }
        return dto;
    }

    /**
     * DTO → Entity
     *
     * Note: we only parse the duration here.  If you need to look up
     *       and assign a real ClassRoom/ExamRoom, do that in your service.
     */
    public Lesson toEntity(LessonDto dto) {
        if (dto == null) return null;

        Lesson lesson = new Lesson();
        // parse ISO-8601 duration (e.g. "PT1H30M")
        //lesson.setDuration(Duration.parse(dto.getDuration()));
        // lesson.setLessonRoom(...) must be done in service
        return lesson;
    }
}