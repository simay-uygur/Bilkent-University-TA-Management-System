package com.example.entity.Tasks;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

public @Embeddable
@Getter
@Setter
// This class is used to create a composite key for the TA_Task entity
class TA_TaskId implements Serializable{
    @Column(name = "task_id")
    private int taskId;

    @Column(name = "ta_id")
    private Long taId;

    @Column(name = "ta_task_id")
    private Long taTaskId;

    // Default constructor
    public TA_TaskId() {
    }

    // Parameterized constructor
    public TA_TaskId(int taskId, Long taId) {
        this.taskId = taskId;
        this.taId = taId;
        this.taTaskId = Long.parseLong(taskId + "" + taId); // Concatenate taskId and taId to create a unique identifier
    }
} 
