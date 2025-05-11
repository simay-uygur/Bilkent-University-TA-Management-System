package com.example.dto;

import com.example.entity.General.Date;
import com.example.entity.Requests.RequestType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private Long requestId;
    private RequestType requestType;
    private String description;
    private String senderName;
    private String receiverName;
    private Date sentTime;
    private boolean isApproved = false;
    private boolean isRejected = false;
    private boolean isPending = true;
    private String courseCode;
    // no TA sender here → breaks recursion
}