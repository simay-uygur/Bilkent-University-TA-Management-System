package com.example.dto;

import lombok.Data;

@Data
public class ProctoringDto {
    private Integer workload;
    private Boolean hasAdjacentExam;
    private Long taId;
    private String name;
    private String surname;
    private String academicLevel;
}