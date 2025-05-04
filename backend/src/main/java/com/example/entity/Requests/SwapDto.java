package com.example.entity.Requests;

import com.example.dto.RequestDto;
import com.example.entity.General.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SwapDto extends RequestDto {
    private Event duration;
    private String examName;
    private int examId;
}
