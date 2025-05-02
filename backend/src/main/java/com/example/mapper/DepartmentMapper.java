package com.example.mapper;

import com.example.dto.DepartmentDto;
import com.example.entity.Courses.Department;
import com.example.entity.General.Faculty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentMapper {

    public DepartmentDto toDto(Department department) {
        if (department == null) return null;

        String facultyCode = department.getFaculty() != null
                ? department.getFaculty().getCode()
                : null;

        return new DepartmentDto(
                department.getName(),
                facultyCode
        );
    }

    public Department toEntity(DepartmentDto dto, Faculty faculty) {
        if (dto == null) return null;

        Department department = new Department();
        department.setName(dto.getCode());
        department.setFaculty(faculty);
        return department;
    }
}