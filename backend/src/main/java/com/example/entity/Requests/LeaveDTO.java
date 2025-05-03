package com.example.entity.Requests;

import com.example.entity.General.Event;

import lombok.Data;

@Data
public class LeaveDTO {
    private RequestType requestType;
    private String description;
    private Event duration;
    private Long receiverId;    
    //private MultipartFile file;   
}
