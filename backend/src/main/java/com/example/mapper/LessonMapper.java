package com.example.mapper;

import com.example.dto.DateDto;
import com.example.dto.EventDto;
import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Date;
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

    private Event toEvent(EventDto dto) {
        if (dto == null) return null;
        return new Event(
                new Date(
                        dto.getStart().getDay(),
                        dto.getStart().getMonth(),
                        dto.getStart().getYear(),
                        dto.getStart().getHour(),
                        dto.getStart().getMinute()
                ),
                new Date(
                        dto.getFinish().getDay(),
                        dto.getFinish().getMonth(),
                        dto.getFinish().getYear(),
                        dto.getFinish().getHour(),
                        dto.getFinish().getMinute()
                )
        );
    }

    private EventDto toEventDto(Event event) {
        return new EventDto(
                new DateDto(
                        event.getStart().getDay() != null ? event.getStart().getDay() : 0,
                        event.getStart().getMonth() != null ? event.getStart().getMonth() : 0,
                        event.getStart().getYear() != null ? event.getStart().getYear() : 0,
                        event.getStart().getHour() != null ? event.getStart().getHour() : 0,
                        event.getStart().getMinute() != null ? event.getStart().getMinute() : 0
                ),
                new DateDto(
                        event.getFinish().getDay() != null ? event.getFinish().getDay() : 0,
                        event.getFinish().getMonth() != null ? event.getFinish().getMonth() : 0,
                        event.getFinish().getYear() != null ? event.getFinish().getYear() : 0,
                        event.getFinish().getHour() != null ? event.getFinish().getHour() : 0,
                        event.getFinish().getMinute() != null ? event.getFinish().getMinute() : 0
                )
        );
    }

}