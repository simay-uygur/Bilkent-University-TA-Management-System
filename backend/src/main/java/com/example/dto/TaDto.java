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
    private Boolean isActive;
    private Boolean isGraduated;
    private String department;
    private List<String> courses;
    private List<String> lessons;
    private List<String> tasks;
    private List<RequestDto> sendedRequests;
    private List<RequestDto> receivedRequests;
    private List<String> tasksAsStudent;
    private List<String> tasksAsTA;
    private String proctorType;
    private String taType;
    //private List<RequestDto> sendedRequests;
}


