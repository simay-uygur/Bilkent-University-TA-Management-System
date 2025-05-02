package com.example.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {

    private String code;         // e.g., “CS”, “IE”
    private String facultyCode;  // parent faculty, no full object to avoid cycles
}