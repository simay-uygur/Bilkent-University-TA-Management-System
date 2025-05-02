package com.example.entity.Exams;

import java.util.List;

import com.example.entity.Tasks.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
    @Column(name = "exam_id", unique = true)
    private int exam_id;

    @Column(name = "description", unique = false, updatable = true)
    private String description;

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
