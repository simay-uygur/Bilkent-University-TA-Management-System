package com.example.mapper;

import org.springframework.stereotype.Component;

import com.example.dto.DateDto;
import com.example.dto.EventDto;
import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Event;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LessonMapper {

    public LessonDto toDto(Lesson lesson) {
        if (lesson == null) return null;

        LessonDto dto = new LessonDto();

        // ISO-8601 duration string (e.g. "PT1H30M")
        dto.setDuration(lesson.getDuration() != null ? lesson.getDuration() : null);

        // classRoom → use classroomId and examCapacity
        ClassRoom room = lesson.getLessonRoom();
        if (room != null) {
            dto.setClassroomId(room.getClassroomId());
            //dto.setExamCapacity(room.getExamCapacity());
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
}