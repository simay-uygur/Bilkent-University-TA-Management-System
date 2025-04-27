package com.example.service;

import com.example.entity.General.Student;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface StudentServ {

    void saveAll(List<Student> students);

    Map<String, Object> importStudentsFromExcel(MultipartFile file);

    Student getStudentById(Long id);

    List<Student> getAllStudents();

    void deleteStudentById(Long id);

    Student saveStudent(Student student);

    Student updateStudent(Long id, Student updatedStudent);
}