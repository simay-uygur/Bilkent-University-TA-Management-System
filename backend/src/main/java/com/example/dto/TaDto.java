package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.entity.Requests.SwapDto;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.entity.Requests.WorkLoadDto;

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
    private List<String> tasks;
    private List<WorkLoadDto> sendedWorkLoadRequests;
    private List<SwapDto> sendedSwapRequests;
    private List<TransferProctoringDto> sendedTransRequests;
    private List<SwapDto> receivedSwapRequests;
    private List<TransferProctoringDto> receivedTransRequests;
    private List<String> tasksAsStudent;
    private List<String> tasksAsTA;
    private String proctorType;
    private String taType;
    //private List<RequestDto> sendedRequests;
}


