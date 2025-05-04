package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private String type;
    private int requestId;
    private String requestType;
    private LocalDateTime sentTime;
    // no TA sender here → breaks recursion
}