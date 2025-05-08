package com.example.entity.Requests;

import java.util.List;

import com.example.dto.RequestDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProctorTaFromFacultiesDto extends RequestDto {
    private Long deansId;
    private List<ProctorTaFromOtherFacultyDto> proctorTaInFacultyDtos;
    private String examName;
    private int    examId;
    private int requiredTas;
    private int tasLeft;
}