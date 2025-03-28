package com.example.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
//@Table(name = "task_table", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "task_id"}))
@Table(name = "task_table")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto id generation
    @Column(name = "task_id", unique = true, updatable = false, nullable = false)
    private int task_id;

    /*@ManyToOne // when using relationships needs to know who // changes did not save in visual paradigm ))))))
    @JoinColumn(name = "course_id")
    private Course course; //many to one*/

    @ManyToMany(mappedBy="ta_tasks_list",fetch = FetchType.LAZY) // mappedby means that the other side is the owner of the relationship(TA)
    @JsonIgnore
    private Set<TA> tas_list = new HashSet<TA>(); //many to many

    /*@ManyToMany(mappedBy="section_tasks_list",fetch = FetchType.LAZY)
    private List<Section> sections_list ; //many to many*/
    
    @Embedded
    @Column(name = "duration", unique = false, updatable = true, nullable = false)
    private Event duration;

    @Column(name = "required_tas", unique = false, updatable = true, nullable = false)
    private int requiredTAs;

    @Column(name = "size_of_tas", unique = false, updatable = true, nullable = false)
    private int amount_of_tas;

    @Column(name = "is_time_passed", unique = false, updatable = true, nullable = false)
    private boolean isTimePassed;

    @Column(name = "workload", unique = false, updatable = true, nullable = false)
    private int workload; 

    @Column(name = "amount_of_workers", unique = false, updatable = true, nullable = false)
    private int size; 

    @Column(name = "type", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType type;
    
    @Column(name = "status", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskState status;

    //Method to check if task is still active
    public boolean isTaskActive() {
        return duration.isOngoing();
    }
}
