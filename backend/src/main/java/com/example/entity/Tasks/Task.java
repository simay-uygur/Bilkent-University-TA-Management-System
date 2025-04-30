package com.example.entity.Tasks;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Courses.Course;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.ManyToOne;
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
@Table(name = "task_table")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE) // auto id generation
    @Column(name = "task_id", unique = true, updatable = true, nullable = false)
    private int task_id;
    
    @Embedded
    @Column(name = "duration", unique = false, updatable = true, nullable = false)
    private Event duration;

    @Column(name = "is_time_passed", unique = false, updatable = true, nullable = false)
    private boolean isTimePassed;

    @Column(name = "workload", unique = false, updatable = true, nullable = false)
    private int workload; 

    @Column(name = "task_type", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType task_type;
    
    @Column(name = "status", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskState status = TaskState.UNKNOWN;

    @Column(name = "access_type", unique = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TaskAccessType access_type;

    //Method to check if task is still active
    public boolean isTaskActive() {
        return duration != null && duration.isOngoing();
    }
    
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<TA_Task> tas_list = new ArrayList<>(); 
    //new class(TA_task) one to many, public task one to many

    @Column(name = "required_tas", unique = false, updatable = true, nullable = false)
    private int requiredTAs;

    @Column(name = "size_of_tas", unique = false, updatable = true, nullable = false)
    private int amount_of_tas = 0;
      
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId")
    private Course course;

    @JsonIgnore
    @OneToOne(
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
        optional = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "exam_id")
    private Exam exam;

    public boolean addTA(){
        if (tas_list == null) // Check if tas_list is null before initializing
            tas_list = new ArrayList<>(); // Initialize the list if it's null
        if (amount_of_tas < requiredTAs) {
            amount_of_tas++;
            return true;
        }
        return false;
    }

    public boolean removeTA(){
        if (amount_of_tas > 0) {
            amount_of_tas--;
            return true;
        }
        return false;
    }

    public String getStart_date() {
        if (duration != null && duration.getStart() != null) {
            return duration.getStart().toString(); // Assuming Event has a toString() method that formats the date correctly
        }
        return null; // or throw an exception if you prefer
    }
}
