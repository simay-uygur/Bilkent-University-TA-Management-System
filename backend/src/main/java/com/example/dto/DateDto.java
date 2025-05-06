package com.example.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateDto {
    private Integer day;    // was int
    private Integer month;  // was int
    private Integer year;   // was int
    private Integer hour;
    private Integer minute;
}