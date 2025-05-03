package com.example.entity.Courses;


import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.General.Semester;
import com.example.entity.General.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Builder
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

    //prerequisites as simple Strings
    @ElementCollection
    @CollectionTable(
      name = "course_offering_prereqs",
      joinColumns = @JoinColumn(name = "offering_id")
    )
    @Column(name = "prereq_code")
    private List<String> prereqs = new ArrayList<>();

    // enrolled students
    @ManyToMany(fetch = LAZY)
    @JoinTable(
      name = "course_offering_students",
      joinColumns = @JoinColumn(name = "offering_id"),
      inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> students = new ArrayList<>();

    // assigned TAs
    @ManyToMany(fetch = LAZY)
    @JoinTable(
      name = "course_offering_tas",
      joinColumns = @JoinColumn(name = "offering_id"),
      inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> tas = new ArrayList<>();

    // course instructors / coordinators
    @ManyToMany(fetch = LAZY)
    @JoinTable(
      name = "course_offering_instructors",
      joinColumns = @JoinColumn(name = "offering_id"),
      inverseJoinColumns = @JoinColumn(name = "instructor_id")
    )
    private List<Instructor> instructors = new ArrayList<>();
}
        

