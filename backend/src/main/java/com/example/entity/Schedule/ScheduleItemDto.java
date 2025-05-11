package com.example.entity.Schedule;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ScheduleItemDto {
    private LocalDate date;
    private int slotIndex; // 1 den 12 kadar
    private String type;  
    private String classroom;      
    private String code; // section veya course code 
    private Long referenceId; 
}
