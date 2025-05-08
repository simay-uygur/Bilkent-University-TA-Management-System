package com.example.dto;


import java.util.List;

import com.example.entity.General.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Task entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private int id;
    private String type;

    /**
     * List of TAs assigned to this task.
     */
    private List<TaDto> tas;

    /**
     * A brief description of the task.
     */
    private String description;

    /**
     * Estimated duration (e.g., "2h", "00:30").
     */
    private Event duration;

    /**
     * Current status (e.g., "PENDING", "COMPLETED").
     */
    private String status;

    private int workload;

    /**
     * Convenience constructor for tasks with no assigned TAs.
     */
    public TaskDto(int id, String type, String description, Event duration, String status, int workload) {
        this(id, type, List.of(), description, duration, status, workload);
    }
}


/*
package com.example.entity.Tasks;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Task_DTO {
    
    private String type;

    private List<TA_DTO> tas;

    private String description;

    private String duration;

    private String status;

    public Task_DTO() {
    }

    // All-args constructor
    public Task_DTO(String type, List<TA_DTO> tas, String description, String duration, String status) {
        this.type = type;
        this.tas = tas;
        this.description = description;
        this.duration = duration;
        this.status = status;
    }

    // Constructor without TAs (initialize empty list)
    public Task_DTO(String type, String description, String duration, String status) {
        this(type, new ArrayList<>(), description, duration, status);
    }
}
*/
