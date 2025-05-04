package com.example.mapper;

import org.springframework.stereotype.Component;

import com.example.dto.RequestDto;
import com.example.entity.Requests.Request;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestMapper {
    
    public RequestDto toDto(Request request) {
        RequestDto dto = new RequestDto();
        dto.setRequestType(request.getRequestType().name());
        dto.setRequestId(request.getRequestId());
        dto.setSentTime(request.getSentTime());
        return dto;
    }
}
