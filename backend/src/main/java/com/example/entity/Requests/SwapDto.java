package com.example.entity.Requests;

import com.example.dto.RequestDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SwapDto extends RequestDto {
    private Long senderId;
    private Long receiverId;
    private String senderExamName;
    private String receiverExamName;
    private int senderExamId;
    private int receiverExamId;
}
