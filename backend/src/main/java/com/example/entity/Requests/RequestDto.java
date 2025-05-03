package com.example.entity.Requests;

import com.example.entity.General.Date;

import lombok.Data;

@Data
public class RequestDto {
    private Long requestId;
    private RequestType requestType;
    private String description;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private Date sentTime;
}
