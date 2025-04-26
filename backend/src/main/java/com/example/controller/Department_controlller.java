package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Courses.Department;
import com.example.service.DepartmentServ;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class Department_controlller {
    
    @Autowired
    private final DepartmentServ depServ;

    @PostMapping("api/dep")
    public ResponseEntity<Boolean> createDepartment(@RequestBody Department dep) {
        return new ResponseEntity<>(depServ.createDepartment(dep),HttpStatus.CREATED);
    }
    
}
