package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseOfferingDto {
    private Long id;
    private CourseDto course;
    private SemesterDto semester;
    private List<SectionDto> sections;
}