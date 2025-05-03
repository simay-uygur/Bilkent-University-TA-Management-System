package com.example.controller;

import com.example.dto.CourseOfferingDto;
import com.example.entity.Courses.CourseOffering;
import com.example.mapper.CourseOfferingMapper;
import com.example.service.CourseOfferingServ;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/offerings")
@RequiredArgsConstructor
public class CourseOfferingController {

    private final CourseOfferingServ service;
    private final CourseOfferingMapper mapper;

    @PostMapping
    public CourseOfferingDto create(@RequestBody CourseOfferingDto dto) {
        CourseOffering entity = mapper.toEntity(dto);
        CourseOffering saved = service.create(entity);
        return mapper.toDto(saved);
    }

    @GetMapping
    public List<CourseOfferingDto> list() {
        return service.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
   
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<CourseOfferingDto>> getOfferingsByDepartment(@PathVariable String deptId) {
        
        return new ResponseEntity<>(service.getOfferingsByDepartment(deptId), HttpStatus.FOUND);

    }
    @GetMapping("/{id}")
    public CourseOfferingDto get(@PathVariable Long id) {
        return mapper.toDto(service.getById(id));
    }

    @PutMapping("/{id}")
    public CourseOfferingDto update(@PathVariable Long id,
                                    @RequestBody CourseOfferingDto dto) {
        CourseOffering entity = mapper.toEntity(dto);
        return mapper.toDto(service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}


//package com.example.controller;
//
//import com.example.entity.Courses.CourseOffering;
//
//import com.example.service.CourseOfferingServ;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/offerings")
//@RequiredArgsConstructor
//public class CourseOfferingController {
//    private final CourseOfferingServ service;
//
//    @PostMapping
//    public CourseOffering create(@RequestBody CourseOffering offering) {
//        return service.create(offering);
//    }
//
//    @GetMapping
//    public List<CourseOffering> list() {
//        return service.getAll();
//    }
//
//    @GetMapping("/{id}")
//    public CourseOffering get(@PathVariable Long id) {
//        return service.getById(id);
//    }
//
//    @PutMapping("/{id}")
//    public CourseOffering update(@PathVariable Long id,
//                                 @RequestBody CourseOffering offering) {
//        return service.update(id, offering);
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Long id) {
//        service.delete(id);
//    }
//}