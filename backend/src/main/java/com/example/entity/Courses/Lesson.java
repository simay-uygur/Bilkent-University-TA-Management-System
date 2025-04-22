package com.example.entity.Courses;

import com.example.entity.General.ClassRoom;
import com.example.entity.General.Event;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lesson_table")
@Getter
@Setter
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lesson_id;

    @Embedded
    @Column(name = "duration")
    private Event duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)  // Makes section mandatory
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")  // Nullable = true by default
    private ClassRoom lesson_room;

    public enum Lesson_Type{
        LESSON,
        SPARE_HOUR
    }

}
