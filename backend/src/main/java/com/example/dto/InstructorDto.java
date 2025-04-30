package com.example.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InstructorDto {
    private Long id;
    private String name;
    private String surname;
    private Boolean isActive;
    private String departmentName;
    private List<String> courseCodes;
}