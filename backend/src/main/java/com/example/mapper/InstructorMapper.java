package com.example.mapper;

import com.example.dto.InstructorDto;
import com.example.entity.Actors.Instructor;
import com.example.entity.Courses.Course;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InstructorMapper {

    public InstructorDto toDto(Instructor instructor) {
        InstructorDto dto = new InstructorDto();
        dto.setId(instructor.getId());
        dto.setName(instructor.getName());
        dto.setSurname(instructor.getSurname());
        dto.setIsActive(instructor.getIsActive());

        if (instructor.getDepartment() != null) {
            dto.setDepartmentName(instructor.getDepartment().getName());
        }

        List<String> courseCodes = instructor.getCourses().stream()
                .map(Course::getCourseCode) // instead of lambda expression
                .collect(Collectors.toList());
        dto.setCourseCodes(courseCodes);

        return dto;
    }
}