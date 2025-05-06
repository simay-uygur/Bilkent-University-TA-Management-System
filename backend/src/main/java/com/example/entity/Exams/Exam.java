package com.example.entity.Exams;

import java.util.List;

import com.example.entity.Tasks.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exam_table")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id", unique = true)
    private int exam_id;

    @Column(name = "description", unique = false, updatable = true)
    private String description; // writing if it is midterm 1, 2 etc (flexible because

    @JsonIgnore
    @OneToOne(
        mappedBy = "exam",  // This refers to the 'exam' field in Task class
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.LAZY,
        optional = false
    )
    private Task task;

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ExamRoom> exam_rooms;
}

//converter is missing