package com.example.entity.Tasks;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Section;
import com.example.entity.General.Event;
import com.example.entity.Requests.WorkLoad;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_table")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "task_id", nullable = false, updatable = true)
    private int taskId;

    @Embedded
    private Event duration;


    /*@Column(name = "is_time_passed", nullable = false)
    private boolean timePassed;*/

    @Column(name = "workload", nullable = false)
    private int workload;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Column(name = "description", nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskState status = TaskState.UNKNOWN;

    /*@Enumerated(EnumType.STRING)
    @Column(name = "access_type", updatable = false)
    private TaskAccessType accessType;*/

    @OneToMany(mappedBy = "task", 
    cascade = CascadeType.ALL , 
    orphanRemoval = true)
    private List<TaTask> tasList = new ArrayList<>();

    /*@Column(name = "required_tas", nullable = false)
    private int requiredTAs;*/

    @Column(name = "size_of_tas", nullable = false)
    private int amountOfTas = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL,  orphanRemoval = true   )
    private List<WorkLoad> workloadList = new ArrayList<>();
    /* ─── helper methods ─────────────────────────── */

    /** true if the task’s Event says it is currently ongoing */
    public boolean isTaskActive() {
        return duration != null && duration.isOngoing();
    }

    public void assignTo(TA ta) 
    {
        if (tasList == null) // Check if tasList is null before initializing
            tasList = new ArrayList<>(); // Initialize the list if it's null
        TaTask link = new TaTask(this, ta);
        // set both sides
        this.getTasList().add(link);
        // update counter
        amountOfTas++;
    }

    public boolean removeTA() {
        if (amountOfTas > 0) {
            amountOfTas--;
            return true;
        }
        return false;
    }

    public String getStartDate() {
        return (duration != null && duration.getStart() != null)
                ? duration.getStart().toString()
                : null;
    }

    public Task(Section section, Event duration,String description, String type, int workload)
    {
        this.section = section;
        this.duration = duration;
        this.taskType = TaskType.valueOf(type);
        if (workload == 0)
        {
            switch (taskType){
                case Lab -> this.workload = 4;
                case Grading -> this.workload = 3;
                case Recitation -> this.workload = 2;
                case Office_Hour -> this.workload = 1;
            }
        }
        else 
            this.workload = workload;
    }
    
    /*@OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<WorkLoad> workLoadRequestList = new ArrayList<>(); // List of WorkLoad objects associated with this task*/

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true; // Check if the objects are the same instance
        if (obj == null || getClass() != obj.getClass()) return false; // Check for null or different class
        Task task = (Task) obj; // Cast to Task
        return this.taskId == task.taskId; // Compare based on taskId
    }
}
