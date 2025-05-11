package com.example.entity.Requests;

import com.example.dto.RequestDto;

import lombok.Data;

@Data
public class ProctorTaInFacultyDto extends RequestDto{
    private String depName;
    private String facultyName;

    private Long dean_id;
    private Long instrId;
    
    private String examName;
    private int examId;
    
    private int requiredTas;
    private int tasLeft;
}
