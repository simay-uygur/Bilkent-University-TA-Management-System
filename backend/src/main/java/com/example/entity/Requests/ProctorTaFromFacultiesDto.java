package com.example.entity.Requests;

import java.util.List;

import lombok.Data;

@Data
public class ProctorTaFromFacultiesDto extends RequestDto {
    private List<ProctorTaInFacultyDto> proctorTaInFacultyDtos;
    private String examName;
    private int    examid;

}