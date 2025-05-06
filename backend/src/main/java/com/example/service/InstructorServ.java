package com.example.service;

import com.example.dto.InstructorDto;
import com.example.entity.Actors.Instructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface InstructorServ {

    Map<String, Object> importInstructorsFromExcel(MultipartFile file) throws IOException;

    Instructor getById(Long id);

    Instructor createInstructor(Instructor instructor);

    void deleteInstructor(Long id);

    Instructor updateInstructor(Long id, Instructor instructor);

    List<Instructor> getAllInstructors();

    List<InstructorDto> getAllInstructorsDto();

    List<InstructorDto> getInstructorsByDepartment(String departmentName);

    InstructorDto getInstructorById(Long id);
}