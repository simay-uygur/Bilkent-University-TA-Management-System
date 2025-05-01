package com.example.mapper;

import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;

import com.example.entity.General.ClassRoom;
import org.springframework.stereotype.Component;


@Component
public class LessonMapper {

    /** Entity → DTO */
    public static LessonDto toDto(Lesson lesson) {
        if (lesson == null) return null;
        LessonDto dto = new LessonDto();
        // convert Duration to ISO-8601 string
        dto.setDuration( lesson.getDuration().toString() );
        ClassRoom room = lesson.getLessonRoom();
        dto.setClassCode( room.getClassCode() );
        dto.setRoom( room.getExamRoom().toString() );
        return dto;
    }

    /** DTO → Entity */

    //lesson has duration but
    public Lesson toEntity(LessonDto dto) {
        if (dto == null) return null;
        Lesson lesson = new Lesson();
        // parse ISO-8601 back into an Event
        com.example.entity.General.Event durationEvent = new com.example.entity.General.Event();
        //durationEvent.setStart(dto.);
        //durationEvent.setEventDuration(Duration.parse(dto.getDuration()));
        lesson.setDuration(durationEvent);

        // Don’t try to look up ClassRoom here—you need the repository in your service:
        //    ClassRoom room = roomRepo.findByClassCode(dto.getClassCode()).orElseThrow(...);
        //    lesson.setLessonRoom(room);

        return lesson;
    }
}

/*

package com.example.mapper;

import com.example.dto.LessonDto;
import com.example.entity.Courses.Lesson;
import com.example.entity.General.ClassRoom;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LessonMapper {


public static LessonDto toDto(Lesson lesson) {
    if (lesson == null) return null;
    LessonDto dto = new LessonDto();
    dto.setDuration(lesson.getDuration().toString());
    ClassRoom room = lesson.getLessonRoom();
    dto.setClassCode(room.getClassCode());
    dto.setRoom(room.getRoomNumber());
    return dto;
}

public static Lesson toEntity(LessonDto dto) {
    if (dto == null) return null;
    Lesson lesson = new Lesson();
    lesson.setDuration(Duration.parse(dto.getDuration()));
    // wiring of lesson.getLessonRoom() must happen in your service
    return lesson;
}
}
 */