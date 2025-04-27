package com.example.controller;

import com.example.entity.Courses.Department;
import com.example.repo.DepartmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepo departmentRepo;

    @PostMapping
    public ResponseEntity<Department> addDepartment(@RequestBody Department department) {
        if (departmentRepo.existsById(department.getName())) {
            return ResponseEntity.badRequest().build();
        }
        Department saved = departmentRepo.save(department);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentRepo.findAll();
        return ResponseEntity.ok(departments);
    }


    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String name) {
        if (!departmentRepo.existsById(name)) {
            return ResponseEntity.notFound().build();
        }
        departmentRepo.deleteById(name);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/{name}")
    public ResponseEntity<Boolean> checkDepartmentExists(@PathVariable String name) {
        boolean exists = departmentRepo.existsById(name);
        return ResponseEntity.ok(exists);
    }
}