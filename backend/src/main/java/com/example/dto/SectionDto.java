package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionDto {
    private Long sectionId;          // was int → now Long
    private String sectionCode;
    private String courseName;
    private List<LessonDto> lessons;
    private InstructorDto instructor;
    private List<TaDto> tas;
    private List<StudentDto> students;
    private List<TaskDto> tasks;
    
}