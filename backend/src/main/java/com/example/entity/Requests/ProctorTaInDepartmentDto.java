package com.example.entity.Requests;

import com.example.dto.RequestDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProctorTaInDepartmentDto extends RequestDto{
    private String depName;
    private Long instrId;
    private String examName;
    private int examId;
    private int requiredTas;
    private int tasLeft;
}
