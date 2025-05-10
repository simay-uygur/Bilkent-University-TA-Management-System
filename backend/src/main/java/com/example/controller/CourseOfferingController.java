package com.example.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.example.dto.*;
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

import com.example.entity.Courses.CourseOffering;
import com.example.entity.General.Event;
import com.example.mapper.CourseOfferingMapper;
import com.example.repo.ClassRoomRepo;
import com.example.repo.CourseOfferingRepo;
import com.example.repo.CourseRepo;
import com.example.service.ClassRoomServ;
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
    private final ClassRoomServ roomServ;

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

    @GetMapping("/{id}")
    public ResponseEntity<CourseOfferingDto> get(@PathVariable Long id) {
        //return mapper.toDto(service.getById(id));
        CourseOfferingDto dto = service.getById(id);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dto, HttpStatus.FOUND);
    }
    @GetMapping("/courseCode/{code}")
    public ResponseEntity<List<CourseOfferingDto>> get(@PathVariable String code) {
        //return mapper.toDto(service.getById(id));
        List<CourseOfferingDto> dtos = service.getCoursesByCourseCode(code);
        if (dtos == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dtos, HttpStatus.FOUND);
    }
    /* @GetMapping("/courseCode/{code}")
    public ResponseEntity <CourseOfferingDto> get(@PathVariable String code) {
        //return mapper.toDto(service.getById(id));
       CourseOfferingDto dto = service.getByCourseCode(code);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dto, HttpStatus.FOUND);
    } */
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
    public CompletableFuture<ResponseEntity<Boolean>> createExam(@RequestBody ExamDto exam, @PathVariable String course_code) {
        return service.createExam(exam, course_code).thenApply(success -> {
            if (success) {
              return ResponseEntity.status(HttpStatus.CREATED).body(true);
            } else {
              return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(false);
            }
        });
    }

    @GetMapping("/course/{course_code}/exam/classrooms")
    public ResponseEntity<List<ClassRoomDto>> getAvailableClassRooms(@RequestBody Event event, @PathVariable String course_code){
        return new ResponseEntity<>(roomServ.getClassRoomsByTime(event),HttpStatus.OK);
    }

    @PostMapping("/course/{course_code}/exam/{exam_id}/tas")
    public CompletableFuture<ResponseEntity<Boolean>> addTAs(@PathVariable String course_code, @PathVariable Integer exam_id, @RequestBody List<Long> tas) {
        return service.addTAs(course_code, exam_id, tas).thenApply(success -> {
            if (success) {
              return ResponseEntity.status(HttpStatus.CREATED).body(true);
            } else {
              return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(false);
            }
        });
    }

    //this is for frontend
    @PostMapping("/{courseCode}/add-exam")
    public CompletableFuture<ResponseEntity<Void>> createExam(
            @PathVariable String courseCode,
            @RequestBody ExamDto dto
    ) {
        return service
                //.createExamWithClassRoomGiven(dto, courseCode)
                .createExam(dto, courseCode) //try another
                .thenApply(success -> {
                    // if you want to return the new exam’s id you could switch to returning a body
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .<Void>build();
                });
    }

    @PostMapping("/{courseCode}/exam/slot-info")
    public CompletableFuture<ExamSlotInfoDto> getExamSlotInfo(
            @PathVariable String courseCode,
            @RequestBody EventDto duration
    ) {
        return service.getExamSlotInfo(courseCode, duration);
    }



}


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