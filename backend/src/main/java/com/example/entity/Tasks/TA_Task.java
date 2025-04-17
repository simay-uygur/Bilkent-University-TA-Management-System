package com.example.entity.Tasks;

import com.example.entity.Actors.TA;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TA_Tasks")
public class TA_Task {
    
    @Id
    @Embedded
    private TA_TaskId id; // composite key

    //from tatask to task
    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(name = "task_id", referencedColumnName = "task_id")
    @OneToMany(mappedBy = "task_id")
    private Task task;
    //from tatask to ta
    @ManyToOne
    @MapsId("taId")
    @JoinColumn(name = "ta_id", referencedColumnName = "id")
    @OneToMany(mappedBy = "ta_id")
    private TA ta_owner; //ta that owns the task

    @Column(name = "access_type", unique = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TaskAccessType type;

    //Parameterized constructor
    public TA_Task(Task task, TA ta_owner, TaskAccessType type, TA_TaskId id){
        this.id = id ;
        this.ta_owner = ta_owner ;
        this.task = task ;
        this.type = type ;
    }
}

