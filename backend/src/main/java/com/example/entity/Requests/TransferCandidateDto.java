package com.example.entity.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferCandidateDto {
    private Long   taId;
    private String taName;
}
