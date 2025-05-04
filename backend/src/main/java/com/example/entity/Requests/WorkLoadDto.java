package com.example.entity.Requests;

import com.example.entity.General.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WorkLoadDto extends RequestDto{
    private int taskId;
    private String taskType;
    private Event duration;
    private int workload;
}
