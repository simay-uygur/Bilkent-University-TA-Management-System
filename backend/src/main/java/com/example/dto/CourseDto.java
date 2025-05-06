package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for Course entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String courseAcademicStatus;
    private String department;
    private List<String> prereqs;
    /* private List<StudentDto> students;
    private List<TaDto> tas;
    
    //private InstructorDto coordinator;
    private List<InstructorDto> instructors;
    private List<SectionDto> sections; */
}