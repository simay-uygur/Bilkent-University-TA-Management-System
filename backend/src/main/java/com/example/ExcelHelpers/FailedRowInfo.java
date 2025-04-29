package com.example.ExcelHelpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailedRowInfo {
    private int rowIndex;
    private String message;
}
