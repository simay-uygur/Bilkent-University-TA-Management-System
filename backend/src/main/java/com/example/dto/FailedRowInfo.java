package com.example.dto;

import lombok.*;

/**
 * DTO class for representing failed rows during Excel import.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FailedRowInfo {
    private int rowNumber;
    private String errorMessage;
}