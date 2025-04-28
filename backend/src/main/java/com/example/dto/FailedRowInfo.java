package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO class for representing failed rows during Excel import.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FailedRowInfo {
    private int rowNumber;
    private String errorMessage;
}