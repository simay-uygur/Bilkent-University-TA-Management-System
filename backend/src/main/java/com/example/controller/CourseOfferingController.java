package com.example.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CourseOfferingDto;
import com.example.dto.ExamDto;
import com.example.entity.Courses.CourseOffering;
import com.example.exception.GeneralExc;
import com.example.mapper.CourseOfferingMapper;
import com.example.repo.CourseOfferingRepo;
import com.example.repo.CourseRepo;
import com.example.service.CourseOfferingServ;
import com.example.service.ExamServ;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/offerings")
@RequiredArgsConstructor
public class CourseOfferingController {

    private final CourseOfferingServ service;
    private final CourseOfferingMapper mapper;
    private final CourseRepo courseRepo;
    private final CourseOfferingRepo courseOfferingRepo;
    private final ExamServ examServ;

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

    /*@GetMapping("/{id}")
    public CourseOfferingDto get(@PathVariable Long id) {
        return mapper.toDto(service.getById(id));
    }*/

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

    @PostMapping("/course/{course_code}/exam")
    public ResponseEntity<Boolean> createExam(@RequestBody ExamDto exam, @PathVariable String course_code) {
        CourseOffering offering = courseOfferingRepo.findByCourse_CourseCode(course_code).
        orElseThrow(() -> new GeneralExc("No current offering found for course: " + course_code));
        int size = offering.getExams().size();
        service.createExam(exam, course_code);
        if (offering.getExams().size() == size) {
            throw new GeneralExc("Exam not created successfully for course: " + course_code);
        }
        return new ResponseEntity<>((offering.getExams().size() == size) ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
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