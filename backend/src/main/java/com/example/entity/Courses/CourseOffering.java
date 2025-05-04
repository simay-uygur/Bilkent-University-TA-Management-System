package com.example.entity.Courses;


import com.example.entity.Actors.TA;
import com.example.entity.General.Semester;
import com.example.entity.General.Student;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;


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
            mappedBy = "offering",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Section> sections = new ArrayList<>();

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "offering_ta_registrations",
            joinColumns = @JoinColumn(name = "offering_id"),
            inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> registeredTas = new ArrayList<>();

    /** TAs who are *assigned* to assist/teach this offering */
    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "offering_ta_assignments",
            joinColumns = @JoinColumn(name = "offering_id"),
            inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> assignedTas = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name               = "offering_student_registrations",
            joinColumns        = @JoinColumn(name = "offering_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> registeredStudents = new ArrayList<>();


}

