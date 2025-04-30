package com.example.controller;

import com.example.dto.DepartmentDto;
import com.example.entity.Courses.Department;
import com.example.mapper.DepartmentMapper;
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
    private final DepartmentMapper departmentMapper;
    
  /*  @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments(@RequestParam(required = false) String name) {
        if (name != null) {
            return departmentRepo.findDepartmentByName(name)
                .map(department -> ResponseEntity.ok(List.of(department)))
                .orElse(ResponseEntity.ok(List.of()));
        }
        return ResponseEntity.ok(departmentRepo.findAll());
    }
    */

    @PutMapping("/{name}")
    public ResponseEntity<Department> updateDepartment(@PathVariable String name, @RequestBody Department department) {
        if (!departmentRepo.existsById(name)) {
            return ResponseEntity.notFound().build();
        }
        department.setName(name);
        Department saved = departmentRepo.save(department);
        return ResponseEntity.ok(saved);
    }

    @PostMapping
    public ResponseEntity<Department> addDepartment(@RequestBody Department department) {
        if (departmentRepo.existsById(department.getName())) {
            return ResponseEntity.badRequest().build();
        }
        Department saved = departmentRepo.save(department);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() { //still recursive
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

    @GetMapping("/dto")
    public ResponseEntity<List<DepartmentDto>> getAllDepartmentsAsDto() {
        List<Department> departments = departmentRepo.findAll();
        List<DepartmentDto> dtos = departments.stream()
                .map(departmentMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}