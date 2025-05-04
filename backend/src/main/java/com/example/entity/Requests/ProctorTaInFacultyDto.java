package com.example.entity.Requests;

import com.example.dto.RequestDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProctorTaInFacultyDto extends RequestDto{
    private String facultyName;
    private String examName;
    private int examId;
    private int requiredTas;
}
