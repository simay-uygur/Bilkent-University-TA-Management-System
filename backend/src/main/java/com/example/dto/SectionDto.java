package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for Section entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionDto {
    private int sectionId;
    private String sectionCode;
    private List<LessonDto> lessons;
    private InstructorDto instructor;   // single, not list
}