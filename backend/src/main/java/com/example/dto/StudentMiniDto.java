package com.example.dto;

import lombok.Data;

@Data
public class StudentMiniDto {
    private Long id;
    private String name;
    private String surname;
    private Boolean isTa;
}
