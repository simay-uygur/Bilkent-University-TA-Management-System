package com.example.entity.Actors;

import java.util.List;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class TA_DTO {
    
    private String name;
    private String surname;
    private Long ta_id;

    private String academic_level;
    private int workload;

    private List<String> courses;
    private List<String> lessons;

    // Default constructor (required for JPA)
    public TA_DTO() {
    }

    // Constructor with all fields
    public TA_DTO(String name, String surname, Long ta_id, String academic_level, 
                 int workload, List<String> courses, List<String> lessons) {
        this.name = name;
        this.surname = surname;
        this.ta_id = ta_id;
        this.academic_level = academic_level;
        this.workload = workload;
        this.courses = courses;
        this.lessons = lessons;
    }
}
