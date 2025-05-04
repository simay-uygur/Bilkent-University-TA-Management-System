package com.example.controller;

import com.example.entity.General.Student;
import com.example.repo.InstructorRepo;
import com.example.repo.StudentRepo;
import com.example.service.StudentServ;
import com.example.exception.StudentNotFoundExc;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class StudentController {

    @Autowired
    private StudentServ studentServ;
    private StudentRepo studentRepo;

    @GetMapping("/api/student/all")
    public List<Student> getAllStudents() {
        return studentServ.getAllStudents();
    }

    @GetMapping("/api/student/{id}")
    public Student getStudentById(@PathVariable Long id) {
        Student student = studentServ.getStudentById(id);
        if (student == null) {
            throw new StudentNotFoundExc(id);
        }
        return student;
    }

    @PutMapping("/api/student/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student updatedStudent) {
        Student savedStudent = studentServ.updateStudent(id, updatedStudent);
        return new ResponseEntity<>(savedStudent, HttpStatus.OK);
    }

    @DeleteMapping("/api/student/{id}")
    public ResponseEntity<HttpStatus> deleteStudentById(@PathVariable Long id) {
        Student student = studentServ.getStudentById(id);
        if (student == null) {
            throw new StudentNotFoundExc(id);
        }
        studentServ.deleteStudentById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //this method should be written
//    @GetMapping("/{id}/sections")
//    public ResponseEntity<List<SectionDto>> getSectionsForStudent(@PathVariable Long id) {
//        return studentRepo.findById(id)
//                .map(student -> {
//                    // Force‑initialize if lazy:
//                    List<Section> sections = student.getOfferings();
//                    // Map to DTO if you want to hide fields
//                    List<SectionDto> dtos = sections.stream()
//                            .map(s -> new SectionDto(s.getSectionId(), s.getSectionCode()))
//                            .collect(Collectors.toList());
//                    return ResponseEntity.ok(dtos);
//                })
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

    @PostMapping("/api/student")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student savedStudent = studentServ.saveStudent(student);
        return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
    }
    /*
    example body
    {
  "studentId": 1,
  "studentName": "John",
  "studentSurname": "Doe"
    }
     */
}