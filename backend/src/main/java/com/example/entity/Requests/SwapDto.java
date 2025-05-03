package com.example.entity.Requests;

import com.example.entity.General.Event;

import lombok.Data;

@Data
public class SwapDto extends RequestDto {
    private Event duration;
    private String examName;
    private int examId;
}
