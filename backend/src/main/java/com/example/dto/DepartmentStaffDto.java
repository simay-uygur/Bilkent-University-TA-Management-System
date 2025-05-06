package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStaffDto {
    private Long id;
    private String name;
    private String surname;
    private Boolean isActive;
    private String departmentName;
}

