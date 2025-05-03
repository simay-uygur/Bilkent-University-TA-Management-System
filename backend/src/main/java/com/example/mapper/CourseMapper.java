package com.example.mapper;

import com.example.dto.CourseDto;
import com.example.entity.Courses.Course;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseDto toDto(Course course) {
        if (course == null) return null;

        CourseDto dto = new CourseDto();
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setDepartment(course.getDepartment().getName());
        dto.setCourseAcademicStatus(course.getCourseAcademicStatus().name());
        dto.setPrereqs(
                Arrays.stream(course.getPrereqList().split("\\s*,\\s*"))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList())
        );
        return dto;
    }

    public Course toEntity(CourseDto dto) {
        if (dto == null) return null;

        Course course = new Course();
        course.setCourseId(dto.getCourseId());
        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        return course;
    }
}