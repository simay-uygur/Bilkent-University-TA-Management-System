package com.example.entity.Courses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.DynamicUpdate;

import com.example.entity.Actors.TA;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.Student;
import com.example.entity.Tasks.Task;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
    @Column(name = "course_id", unique = true, updatable = true)
    private int course_id ; 
    
    @Column(name = "course_code", unique = true)
    private String course_code ; 

    

    // cs-319. id -> 'c' + 's' + 319 -> 319319
    @PrePersist
    private void setCourseId() {
        if (this.course_code != null)
            this.course_id = new CourseCodeConverter().code_to_id(this.course_code);
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "course_academic_status", unique = false, updatable = true, nullable = false)
    private AcademicLevelType course_academic_status ;

    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "course_dep", unique = false, updatable = true)
    private String course_dep ;

    //there should be exam class

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable( // creates a table for many to many relationship
        name = "students_list_table",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> students_list = new HashSet<>();

    @Column(name = "prereq_list", unique = false, updatable = true, nullable = false)
    @NotEmpty(message = "The field can not be empty!")
    private String prereq_list;
    // do not use join table

    @OneToMany(
        mappedBy = "course", // the other side of the relationship is the owner of the relationship
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<Section> sections_list ; // this is the list of sections that are related to the course

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
        name = "course_tas",
        joinColumns = @JoinColumn(name = "section_id"),
        inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> course_tas; // tas is the list of tas that are in the section

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj ;
        return course.getCourse_id() == this.course_id;
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