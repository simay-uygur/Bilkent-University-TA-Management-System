package com.example.entity.Courses;

import com.example.entity.General.Event;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Lesson_DTO {
    private String duration ;

    private String room;

    // Default constructor (required for JPA)
    public Lesson_DTO() {
    }

    // All-args constructor
    public Lesson_DTO(Event duration, String room) {
        this.duration = duration.toString();
        this.room = room;
    }
}
