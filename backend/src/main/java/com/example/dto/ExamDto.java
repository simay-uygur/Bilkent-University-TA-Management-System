package com.example.dto;

import java.util.List;

import com.example.entity.General.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDto {
    private Event    duration;
    private String courseCode;
    private String       type;
    private List<String> examRooms;
    private int   requiredTas;
    private Integer workload;
}
