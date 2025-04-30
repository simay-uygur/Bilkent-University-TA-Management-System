package com.example.mapper;

import com.example.dto.CourseDto;
import com.example.dto.DepartmentDto;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Department;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DepartmentMapper {

    public DepartmentDto toDto(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setName(department.getName());

        List<CourseDto> courseDtos = department.getCourses().stream()
                .map(this::toCourseDto)
                .collect(Collectors.toList());

        dto.setCourses(courseDtos);
        return dto;
    }

    private CourseDto toCourseDto(Course course) {
        CourseDto cd = new CourseDto();
        cd.setCourseId(course.getCourseId());
        cd.setCourseName(course.getCourseName());
        cd.setDepartment(course.getDepartment().getName());
        return cd;
    }
}