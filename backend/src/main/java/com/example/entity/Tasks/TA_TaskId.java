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
    private int id1; // taskId

    private Long id2; // taId

    @Column(name = "ta_task_id")
    private Long taTaskId;

    // Default constructor
    public TA_TaskId() {
    }

    // Parameterized constructor
    public TA_TaskId(int taskId, Long taId) {
        this.id1 = taskId;
        this.id2 = taId;
        //this.taTaskId = Long.parseLong(taskId + "" + taId); // Concatenate taskId and taId to create a unique identifier
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TA_TaskId that = (TA_TaskId) o;
        return id1 == that.id1 && Objects.equals(id2, that.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }
} 
