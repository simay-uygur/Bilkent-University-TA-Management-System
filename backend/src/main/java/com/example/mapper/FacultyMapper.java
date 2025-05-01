package com.example.mapper;

import com.example.dto.DepartmentDto;
import com.example.dto.FacultyDto;
import com.example.entity.General.Faculty;
import com.example.entity.Courses.Department;

import java.util.List;
import java.util.stream.Collectors;

public class FacultyMapper {

    public static FacultyDto toDto(Faculty faculty) {
        if (faculty == null) return null;

        List<DepartmentDto> departmentDtos = faculty.getDepartments() != null
                ? faculty.getDepartments().stream()
                .map(DepartmentMapper::toDto)
                .collect(Collectors.toList())
                : null;

        return new FacultyDto(
                faculty.getCode(),
                faculty.getTitle(),
                departmentDtos
        );
    }

    public static Faculty toEntity(FacultyDto dto) {
        if (dto == null) return null;

        Faculty faculty = new Faculty();
        faculty.setCode(dto.getCode());
        faculty.setTitle(dto.getTitle());
        // departments are ignored on purpose to prevent circular dependencies
        return faculty;
    }
}