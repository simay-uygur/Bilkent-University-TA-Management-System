package com.example.mapper;

import com.example.dto.DepartmentDto;
import com.example.dto.FacultyDto;
import com.example.entity.General.Faculty;
import com.example.entity.Courses.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FacultyMapper {

    private final DepartmentMapper departmentMapper;

    public FacultyDto toDto(Faculty faculty) {
        if (faculty == null) return null;

        List<DepartmentDto> departmentDtos = Optional.ofNullable(faculty.getDepartments())
                .orElse(Collections.emptyList())
                .stream()
                .map(departmentMapper::toDto)
                .collect(Collectors.toList());

        return new FacultyDto(
                faculty.getCode(),
                faculty.getTitle(),
                departmentDtos
        );
    }
    public Faculty toEntity(FacultyDto dto) {
        if (dto == null) return null;

        Faculty faculty = new Faculty();
        faculty.setCode(dto.getCode());
        faculty.setTitle(dto.getTitle());
        // departments are ignored on purpose to prevent circular dependencies
        return faculty;
    }
}