package com.example.entity.Courses;


import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Event;
import com.example.entity.General.Semester;
import com.example.entity.General.Student;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
//@Table(name = "course_offerings") -changed
@Table(
        name = "course_offerings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "semester_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Builder
public class CourseOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @OneToMany(
        mappedBy = "courseOffering",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY)
    private List<Exam> exams = new ArrayList<>();

    @OneToMany(
            mappedBy = "offering",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Section> sections = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "offering_ta_registrations",
            joinColumns = @JoinColumn(name = "offering_id"),
            inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    @OrderBy("surname ASC, name ASC")
    @OrderBy("surname ASC, name ASC")
    private List<TA> registeredTas = new ArrayList<>();

    /** TAs who are *assigned* to assist/teach this offering */
    @ManyToMany(fetch =  FetchType.LAZY)
    @JoinTable(
            name = "offering_ta_assignments",
            joinColumns = @JoinColumn(name = "offering_id"),
            inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    @OrderBy("totalWorkload ASC")
    @OrderBy("totalWorkload ASC")
    private List<TA> assignedTas = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name               = "offering_student_registrations",
            joinColumns        = @JoinColumn(name = "offering_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @OrderBy("studentSurname ASC, studentName ASC")
    @OrderBy("studentSurname ASC, studentName ASC")
    private List<Student> registeredStudents = new ArrayList<>();

    @ManyToOne(fetch =  FetchType.LAZY )
    @JoinColumn(name = "coordinator_id", nullable = true, updatable = true)  // to test it now, nullable is true (database will be changed)
    private Instructor coordinator;

    @Column(name = "semester_duration", nullable = false, updatable = true)
    private Event semesterDuration; // the duration of the semester in which this offering is held
}

