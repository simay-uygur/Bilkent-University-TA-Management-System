package com.example.entity.General;


import com.example.entity.Exams.ExamRoom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "class_room")
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "classroom_id", unique = true, updatable = true, nullable = false)
    private int classroom_id; // ex. 3191, 3192 etc, where 1 is the exam room number

    @Column(nullable = false)
    private String class_code;

    @Column(nullable = false)
    private int class_capacity;

    @OneToOne(mappedBy = "exam_room")
    private ExamRoom section_exam; // this is the section that the class room is related to, not the course
}
