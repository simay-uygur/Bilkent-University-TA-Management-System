package com.example.entity.Requests;

import com.example.entity.General.Event;

import lombok.Data;

@Data
public class ProctorTaInFacultyDto extends RequestDto{
    private String facultyName;
    private String examName;
    private int examId;
    private int requiredTas;
}
