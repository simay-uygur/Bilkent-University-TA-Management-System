package com.example.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Course { //why all features have updatable=true? i think some of them should not be updatable especially ids
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "phd_status", unique = true, updatable = true)
    private boolean PHD_status = false ;

    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "ms_status", unique = true, updatable = true)
    private boolean MS_status = false ;

    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "course_dep", unique = true, updatable = true)
    private String course_dep ;

    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "course_id", unique = true, updatable = true)
    private Long course_id ;

    @Embedded
    private List<Event> exams ;

    /*@OneToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE} // course -> deleted => tasks are deleted
    )
    @JoinTable( // creates a table for one to many relationship
        name = "course_tasks",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id") // creates in ta table field for tasks 
    )
    private Set<Task> course_tasks_list = new HashSet<Task>();  */  
}

/*

@Entity
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "PhD status cannot be null")
    @Column(name = "phd_status")
    private Boolean PHD_status = false;

    @NotNull(message = "MS status cannot be null")
    @Column(name = "ms_status")
    private Boolean MS_status = false;

    @NotEmpty(message = "Course department cannot be empty")
    @Column(name = "course_dep")
    private String course_dep;

    @NotNull(message = "Course ID cannot be null")
    @Column(name = "course_id")
    private Long course_id;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Event> exams;
}

 */