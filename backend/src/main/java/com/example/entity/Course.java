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
public class Course {
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
