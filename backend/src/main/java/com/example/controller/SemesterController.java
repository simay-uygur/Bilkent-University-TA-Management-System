package com.example.controller;


import com.example.entity.General.Semester;
import com.example.service.SemesterServ;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
public class SemesterController {
    private final SemesterServ semesterServ;

    @PostMapping
    public Semester create(@RequestBody Semester semester) {
        return semesterServ.create(semester);
    }

    @PutMapping("/{id}")
    public Semester update(@PathVariable Long id,
                           @RequestBody Semester semester) {
        return semesterServ.update(Math.toIntExact(id), semester);
    }

    @GetMapping("/{id}")
    public Semester getById(@PathVariable Long id) {
        return semesterServ.getById(id);
    }

    @GetMapping
    public List<Semester> getAll() {
        return semesterServ.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        semesterServ.delete(id);
    }
}