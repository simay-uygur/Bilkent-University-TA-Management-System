package com.example.entity.Tasks;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.TA_DTO;

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
