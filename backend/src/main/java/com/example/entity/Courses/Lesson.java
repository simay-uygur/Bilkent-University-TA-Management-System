package com.example.entity.Courses;

import com.example.entity.General.ClassRoom;
import com.example.entity.General.DayOfWeek;
import com.example.entity.General.Event;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * One weekly lesson hour (time-slot) of a Section.
 */
@Entity
@Table(name = "lesson_table")
@Getter
@Setter
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "duration")
    private Event duration;                       // start / end date-times

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;                      // each lesson belongs to exactly one section

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")            // nullable=true (default)
    private ClassRoom lessonRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false)
    private LessonType lessonType;

//    public enum LessonType {
//        LESSON,
//        SPARE_HOUR,
//        LAB,
//        RECITATION,
//        OFFICE_HOUR
//    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
    public enum LessonType {
        LESSON,
        SPARE_HOUR,
        LAB,
        RECITATION,
        OFFICE_HOUR;

        @JsonCreator
        public static LessonType from(String key) {
            return LessonType.valueOf(key.trim().toUpperCase());
        }
    }


    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek day;

}

