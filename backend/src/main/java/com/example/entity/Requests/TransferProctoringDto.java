package com.example.entity.Requests;

import com.example.entity.General.Event;

import lombok.Data;

@Data
public class TransferProctoringDto extends RequestDto {
    private Event duration;
    private String examName;
    private int examId;
    private int requiredTas;
}
