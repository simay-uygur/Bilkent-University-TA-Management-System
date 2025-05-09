package com.example.dto;

import java.util.List;

import com.example.entity.Requests.RequestType;

import lombok.Data;

@Data
public class TAAssignmentRequest {
    private List<Long> preferredTas;
    private List<Long> nonPreferredTas;
    private int taNeeded;
    private RequestType request_type;
}
