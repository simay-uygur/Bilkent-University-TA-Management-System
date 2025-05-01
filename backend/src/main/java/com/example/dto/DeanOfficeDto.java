package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Flat representation of a Dean’s Office user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeanOfficeDto {

    private Long   id;

    private String name;

    private String surname;

    private String webmail;

    private String facultyCode;   // the Faculty this dean’s office belongs to
}