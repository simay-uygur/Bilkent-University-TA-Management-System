package com.example.mapper;

import com.example.dto.InstructorDto;
import com.example.entity.Actors.Instructor;
import com.example.entity.Courses.Course;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class InstructorMapper {
    public InstructorDto toDto(Instructor instructor) {
        InstructorDto dto = new InstructorDto();
        dto.setId(instructor.getId());
        dto.setName(instructor.getName());
        dto.setSurname(instructor.getSurname());
        if (instructor.getDepartment() != null) {
            dto.setDepartmentName(instructor.getDepartment().getName());
        }
        dto.setCourseCodes(
                instructor.getCourses().stream()
                        .map(Course::getCourseCode)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    public Instructor toEntity(InstructorDto dto) {
        Instructor inst = new Instructor();
        inst.setId(dto.getId());
        inst.setName(dto.getName());
        inst.setSurname(dto.getSurname());
        // department & courses wiring in service
        return inst;
    }
}