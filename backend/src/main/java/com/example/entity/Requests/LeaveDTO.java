package com.example.entity.Requests;

import com.example.entity.General.Date;
import com.example.entity.General.Event;

import lombok.Data;

@Data
public class LeaveDTO {
    private Long requestId;
    private RequestType requestType;
    private String description;
    private Date sentTime;
    private Long receiverId;
    private Event duration;
}
