package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaDto {
    private Long id;
    private String name;
    private String surname;
    private String academicLevel;
    private int totalWorkload;
    private boolean isActive;
    private boolean isGraduated;
    private String department;
    private List<String> courses;
    private List<String> lessons;
}


