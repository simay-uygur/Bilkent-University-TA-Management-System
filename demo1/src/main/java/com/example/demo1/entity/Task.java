package com.example.demo1.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto id generation
    private int task_id;

    @ManyToOne // when using relationships needs to know who // changes did not save in visual paradigm ))))))
    @JoinColumn(name = "course_id")
    private Course course; //many to one

    @ManyToMany(mappedBy="ta_tasks_list",fetch = FetchType.LAZY) // mappedby means that the other side is the owner of the relationship(TA)
    private List<TA> tas_list ; //many to many

    @ManyToMany(mappedBy="section_tasks_list",fetch = FetchType.LAZY)
    private List<Section> sections_list ; //many to many

    @Column(name = "is_approved", unique = false, updatable = true, nullable = false)
    private boolean isApproved;
    
    @Embedded
    private Event duration;

    public boolean isTaskActive(Date currentDate) {
        return duration.isOngoing(currentDate);
    }

    @Column(name = "is_time_passed", unique = false, updatable = true, nullable = false)
    private boolean isTimePassed;

    @Column(name = "workload", unique = false, updatable = true, nullable = false)
    private int workload; 

    @Column(name = "amount_of_workers", unique = false, updatable = true, nullable = false)
    private int size; 

    @Column(name = "type", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType type;

    private void mark_approved() {
        this.isApproved = true;
        for (TA ta : tas_list) {
            ta.getTa_tasks_list().remove(this);
        }
        //tas_list.clear();
    }

    public void approve(){
        mark_approved();
    }
}
