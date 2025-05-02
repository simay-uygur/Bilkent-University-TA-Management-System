// com/example/controller/CourseOfferingController.java
package com.example.controller;

import com.example.entity.Courses.CourseOffering;

import com.example.service.CourseOfferingServ;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offerings")
@RequiredArgsConstructor
public class CourseOfferingController {
    private final CourseOfferingServ service;

    @PostMapping
    public CourseOffering create(@RequestBody CourseOffering offering) {
        return service.create(offering);
    }

    @GetMapping
    public List<CourseOffering> list() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public CourseOffering get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public CourseOffering update(@PathVariable Long id,
                                 @RequestBody CourseOffering offering) {
        return service.update(id, offering);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}