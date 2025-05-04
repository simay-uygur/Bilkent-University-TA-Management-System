package com.example.entity.Requests;

import com.example.entity.General.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SwapEnableDto extends RequestDto {
    private Event duration;
    private String examName;
    private int examId;
}
