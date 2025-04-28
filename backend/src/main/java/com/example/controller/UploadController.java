package com.example.controller;

import com.example.service.CourseServ;
import com.example.service.StudentServ;
import com.example.service.TAServ;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController {
    @Autowired
    private StudentServ studentServ;

    @Autowired
    private TAServ taServ;

    @Autowired
    private CourseServ courseServ;

    //for uploading students and ta's
    @PostMapping("/students")
    public ResponseEntity<Map<String, Object>> uploadStudents(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = studentServ.importStudentsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    //for uploading only  TA's from excel file' - not used anymore   - -
    @PostMapping("/tas")
    public ResponseEntity<Map<String, Object>> uploadTAs(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = taServ.importTAsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PostMapping("/api/upload/courses")
    public ResponseEntity<Map<String, Object>> uploadCourses(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = courseServ.importCoursesFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}