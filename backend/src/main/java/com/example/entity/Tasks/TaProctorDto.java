package com.example.entity.Tasks;

import com.example.entity.General.Date;

import lombok.Data;

@Data
public class TaProctorDto {
    private String courseCode;
    private Date start;
    private Date finish;
    private String examType;
}
