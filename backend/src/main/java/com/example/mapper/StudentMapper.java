
package com.example.mapper;

import com.example.dto.StudentDto;
import com.example.entity.General.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentMapper {
    public StudentDto toDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setStudentId(student.getStudentId());
        dto.setStudentName(student.getStudentName());
        dto.setStudentSurname(student.getStudentSurname());
        dto.setWebmail(student.getWebmail());
        dto.setAcademicStatus(student.getAcademicStatus());
        dto.setDepartment(student.getDepartment());
        dto.setIsActive(student.getIsActive());
        dto.setIsGraduated(student.getIsGraduated());
        return dto;
    }
}

