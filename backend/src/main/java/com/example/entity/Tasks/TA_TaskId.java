package com.example.entity.Tasks;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

public 
@Embeddable
@Getter
@Setter
// This class is used to create a composite key for the TA_Task entity
class TA_TaskId implements Serializable{
    private int taskId;

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
        //this.taTaskId = Long.parseLong(taskId + "" + taId); // Concatenate taskId and taId to create a unique identifier
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TA_TaskId that = (TA_TaskId) o;
        return taskId == that.taskId && Objects.equals(taId, that.taId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, taId);
    }
} 
