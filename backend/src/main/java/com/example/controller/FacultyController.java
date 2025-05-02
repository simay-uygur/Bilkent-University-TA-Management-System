package com.example.controller;


import com.example.entity.General.Faculty;
import com.example.service.FacultyServ;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyServ facultyServ;

    @PostMapping
    public Faculty create(@RequestBody Faculty faculty) {
        return facultyServ.save(faculty);
    }

    @GetMapping
    public List<Faculty> list() {
        return facultyServ.getAll();
    }

    @GetMapping("/{code}")
    public Faculty byCode(@PathVariable String code) {
        return facultyServ.getByCode(code);
    }

    @DeleteMapping("/{code}")
    public void delete(@PathVariable String code) {
        facultyServ.deleteByCode(code);
    }
}


