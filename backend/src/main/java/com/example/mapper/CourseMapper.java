package com.example.mapper;

import com.example.dto.CourseDto;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Department;
import com.example.entity.General.AcademicLevelType;
import com.example.repo.DepartmentRepo;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final DepartmentRepo departmentRepo;
    private final DepartmentMapper departmentMapper;

    public CourseDto toDto(Course course) {
        if (course == null) return null;

        CourseDto dto = new CourseDto();
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setDepartment(course.getDepartment().getName());
        //dto.setDepartment((departmentMapper.toDto(course.getDepartment())).getCode());

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
        course.setCourseAcademicStatus(AcademicLevelType.valueOf(dto.getCourseAcademicStatus()));
        Department department = departmentRepo.findDepartmentByName(dto.getDepartment())
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + dto.getDepartment()));
        course.setDepartment(department);

        // section is not set  - - (no need?)
        return course;
    }
}