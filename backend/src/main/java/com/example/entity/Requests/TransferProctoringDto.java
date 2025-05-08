package com.example.entity.Requests;

import com.example.dto.RequestDto;
import com.example.entity.General.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TransferProctoringDto extends RequestDto {
    private Long senderId;
    private Long receiverId;
    private Event duration;
    private String examName;
    private int examId;
}
