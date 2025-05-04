package com.example.entity.Requests;

import java.util.List;

import com.example.dto.TaskDto;
import com.example.entity.General.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LeaveDTO extends RequestDto {
    private Event duration;
    private boolean isPending;
    private List<TaskDto> tasks;

    private String attachmentFilename;
    private String attachmentContentType;
    private String attachmentUrl;
}
