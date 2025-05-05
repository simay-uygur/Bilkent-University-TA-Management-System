package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDto {

    private Long   id;             // instructorId
    private String name;
    private String surname;
    private String webmail;        // added
    private String departmentName;
    private List<String> sections;
}

/*
package com.example.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDto {
    private Long id;
    private String name;
    private String surname;
    private Boolean isActive;
    private String departmentName;
    private List<String> courseCodes;
}*/
