package com.example.entity.Courses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import com.example.entity.Actors.TA;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.Student;
import com.example.entity.Tasks.Task;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Table(name = "course")
@DynamicUpdate // this is used to update only the changed fields in the database, not the whole object
public class Course {
    @Id
    @Column(name = "courseId", unique = true, updatable = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int courseId;
    
    @Column(name = "course_code", unique = true)
    private String courseCode; // cs-319

    @Column(name = "course_name", unique = false, updatable = true, nullable = false)
    //@NotEmpty(message = "The field can not be empty!")
    private String courseName;

    // cs-319. id -> 'c' + 's' + 319 -> 319319
/*
    @PrePersist //before
    private void setCourseId() {
        if (this.courseCode != null)
            this.courseId = new CourseCodeConverter().code_to_id(this.courseCode.toLowerCase()); //may be changed
    }
*/

    @Enumerated(EnumType.STRING)
    @Column(name = "course_academic_status", updatable = true, nullable = false)
    private AcademicLevelType courseAcademicStatus;

    /*@NotEmpty(message = "The field can not be empty!")
    @Column(name = "course_dep", unique = false, updatable = true)
    private String course_dep ;*/

    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    //there should be exam class

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable( // creates a table for many to many relationship
        name = "students_list_table",
        joinColumns = @JoinColumn(name = "courseId"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> studentsList = new HashSet<>();

    @Column(name = "prereq_list", unique = false, updatable = true, nullable = false)
    //@NotEmpty(message = "The field can not be empty!")
    private String prereqList;
    // do not use join table

    @OneToMany(
        mappedBy = "course", // the other side of the relationship is the owner of the relationship
        fetch = FetchType.LAZY,
        orphanRemoval = true,
        cascade= CascadeType.ALL
    )
    private List<Section> sectionsList; // this is the list of sections that are related to the course

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
        name = "course_tas",
        joinColumns = @JoinColumn(name = "section_id"),
        inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> courseTas; // tas is the list of tas that are in the section

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj ;
        return course.getCourseId() == this.courseId;
    }

    @OneToMany(
        mappedBy = "course",
        fetch    = FetchType.LAZY,
        cascade  = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Task> tasks = new ArrayList<>();
}
// only one prepersist call method