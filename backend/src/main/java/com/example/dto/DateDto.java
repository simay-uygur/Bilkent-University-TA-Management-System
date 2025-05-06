package com.example.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateDto {
    private int day;
    private int month;
    private int year;
    private int hour;
    private int minute;
}