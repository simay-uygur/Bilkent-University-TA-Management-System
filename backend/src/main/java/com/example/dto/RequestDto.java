package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.example.entity.General.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private String type;
    private Long requestId;
    private String requestType;
    private Date sentTime;
    // no TA sender here → breaks recursion
}