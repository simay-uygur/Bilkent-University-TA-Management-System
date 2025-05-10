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
    private int examId;
    private Event    duration;
    private String courseCode;
    private String       type;
    private List<String> examRooms;
    private int   requiredTas;
    private Integer workload;

    public ExamDto(Event duration, String courseCode, String type, List<String> examRooms, int reqTas, Integer workload){
        this.duration = duration;
        this.courseCode = courseCode;
        this.type = type;
        this.examRooms = examRooms;
        this.requiredTas = reqTas;
        this.workload = workload;
    }
}
