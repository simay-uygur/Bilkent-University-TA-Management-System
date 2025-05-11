package com.example.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.DepartmentDto;
import com.example.entity.Courses.Department;
import com.example.mapper.DepartmentMapper;
import com.example.repo.DepartmentRepo;
import com.example.service.ExamServ;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepo departmentRepo;
    private final DepartmentMapper   departmentMapper;
    private final ExamServ examService;
    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        List<DepartmentDto> dtos = departmentRepo.findAll().stream()
                .map(departmentMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<DepartmentDto> addDepartment(@RequestBody DepartmentDto dto) {
        if (departmentRepo.existsById(dto.getCode())) {
            return ResponseEntity.badRequest().build();
        }
        Department entity = departmentMapper.toEntity(dto, null);
        Department saved = departmentRepo.save(entity);
        return ResponseEntity.ok(departmentMapper.toDto(saved));
    }
/* @GetMapping("/{examid}")
    public ResponseEntity<String> addExam(@PathVariable Long examid) {

        return examService.getById(examid);
    } */
    @PutMapping("/{code}")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @PathVariable("code") String code,
            @RequestBody DepartmentDto dto) {

        if (!departmentRepo.existsById(code)) {
            return ResponseEntity.notFound().build();
        }
        // rebuild from DTO (faculty can be set later if needed)
        Department entity = departmentMapper.toEntity(dto, null);
        // make sure we don’t accidentally change the PK
        entity.setName(code);

        Department saved = departmentRepo.save(entity);
        return ResponseEntity.ok(departmentMapper.toDto(saved));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable("code") String code) {
        if (!departmentRepo.existsById(code)) {
            return ResponseEntity.notFound().build();
        }
        departmentRepo.deleteById(code);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping
//    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
//        List<Department> departments = departmentRepo.findAll();
//        List<DepartmentDto> dtos = departments.stream()
//                .map(DepartmentMapper::toDto)
//                .toList();
//        return ResponseEntity.ok(dtos);
//    }
//
//    @PostMapping
//    public ResponseEntity<DepartmentDto> addDepartment(@RequestBody DepartmentDto dto) {
//        if (departmentRepo.existsById(dto.getCode())) {
//            return ResponseEntity.badRequest().build();
//        }
//        Department department = DepartmentMapper.toEntity(dto, null); // faculty set separately if needed
//        Department saved = departmentRepo.save(department);
//        return ResponseEntity.ok(DepartmentMapper.toDto(saved));
//    }
//
//    @PutMapping("/{name}")
//    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable String name, @RequestBody DepartmentDto dto) {
//        if (!departmentRepo.existsById(name)) {
//            return ResponseEntity.notFound().build();
//        }
//        Department entity = DepartmentMapper.toEntity(dto, null);
//        entity.setName(name);
//        Department saved = departmentRepo.save(entity);
//        return ResponseEntity.ok(DepartmentMapper.toDto(saved));
//    }
//
//    @DeleteMapping("/{name}")
//    public ResponseEntity<Void> deleteDepartment(@PathVariable String name) {
//        if (!departmentRepo.existsById(name)) {
//            return ResponseEntity.notFound().build();
//        }
//        departmentRepo.deleteById(name);
//        return ResponseEntity.noContent().build();
//    }

    @GetMapping("/{name}")
    public ResponseEntity<Boolean> checkDepartmentExists(@PathVariable String name) {
        boolean exists = departmentRepo.existsById(name);
        return ResponseEntity.ok(exists);
    }
}

/*
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

  */

/*  @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments(@RequestParam(required = false) String name) {
        if (name != null) {
            return departmentRepo.findDepartmentByName(name)
                .map(department -> ResponseEntity.ok(List.of(department)))
                .orElse(ResponseEntity.ok(List.of()));
        }
        return ResponseEntity.ok(departmentRepo.findAll());
    }
    *//*
*/
/*


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

}*/

/*

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

    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        List<Department> departments = departmentRepo.findAll();
        List<DepartmentDto> dtos = departments.stream()
                .map(DepartmentMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<DepartmentDto> addDepartment(@RequestBody DepartmentDto dto) {
        if (departmentRepo.existsById(dto.getCode())) {
            return ResponseEntity.badRequest().build();
        }
        Department department = DepartmentMapper.toEntity(dto, null); // faculty set separately if needed
        Department saved = departmentRepo.save(department);
        return ResponseEntity.ok(DepartmentMapper.toDto(saved));
    }

    @PutMapping("/{name}")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable String name, @RequestBody DepartmentDto dto) {
        if (!departmentRepo.existsById(name)) {
            return ResponseEntity.notFound().build();
        }
        Department entity = DepartmentMapper.toEntity(dto, null);
        entity.setName(name);
        Department saved = departmentRepo.save(entity);
        return ResponseEntity.ok(DepartmentMapper.toDto(saved));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String name) {
        if (!departmentRepo.existsById(name)) {
            return ResponseEntity.notFound().build();
        }
        departmentRepo.deleteById(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Boolean> checkDepartmentExists(@PathVariable String name) {
        boolean exists = departmentRepo.existsById(name);
        return ResponseEntity.ok(exists);
    }
}*/
