package com.example.dto;

import com.example.entity.General.Event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SwapOptionDto {
    private Long   taId;
    private String taName;
    private Integer examId;
    private Event  examDuration;
}
