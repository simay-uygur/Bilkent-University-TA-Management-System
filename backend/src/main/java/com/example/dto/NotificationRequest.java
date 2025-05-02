package com.example.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class NotificationRequest {
    private String title;
    private String text;
    private String receiverName;
}
