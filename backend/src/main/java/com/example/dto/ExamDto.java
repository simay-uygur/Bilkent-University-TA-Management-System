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
    private Event duration;            // start/finish
    private String type;               // örn. "FINAL"
    private List<String> examRooms;    // ["A-104","A-105","A-106"]
    private Integer requiredTas;           // 6
    private Boolean swapIsEnabled;     // true
    private Integer workload;              // 4
}
