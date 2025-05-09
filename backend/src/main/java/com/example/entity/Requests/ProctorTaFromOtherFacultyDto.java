package com.example.entity.Requests;

import com.example.dto.RequestDto;

import lombok.Data;

@Data
public class ProctorTaFromOtherFacultyDto extends RequestDto{
    private Long senderId;
    private Long receiverId;
    private String examName;
    private int    examId;
    private int requiredTas;
    private int tasLeft;
}
