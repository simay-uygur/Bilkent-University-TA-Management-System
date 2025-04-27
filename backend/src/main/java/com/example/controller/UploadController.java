package com.example.controller;

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

    //for uploading TA's from excel file'
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
}