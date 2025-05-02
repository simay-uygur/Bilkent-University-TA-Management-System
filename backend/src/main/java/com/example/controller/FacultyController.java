package com.example.controller;

import com.example.dto.FacultyDto;
import com.example.entity.General.Faculty;
import com.example.mapper.FacultyMapper;
import com.example.service.FacultyServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyServ facultyServ;
    private final FacultyMapper   facultyMapper;

    @PostMapping
    public ResponseEntity<FacultyDto> create(@RequestBody FacultyDto facultyDto) {
        // map DTO → entity, save, then map back
        Faculty saved = facultyServ.save(facultyMapper.toEntity(facultyDto));
        return ResponseEntity.ok(facultyMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<FacultyDto>> list() {
        List<FacultyDto> dtos = facultyServ.getAll().stream()
                .map(facultyMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{code}")
    public ResponseEntity<FacultyDto> byCode(@PathVariable String code) {
        Faculty faculty = facultyServ.getByCode(code);
        return ResponseEntity.ok(facultyMapper.toDto(faculty));
    }

    @PutMapping("/{code}")
    public ResponseEntity<FacultyDto> update(
            @PathVariable String code,
            @RequestBody FacultyDto facultyDto) {

        // fail early if not exists
        facultyServ.getByCode(code);

        // rebuild entity (keep PK unchanged)
        Faculty toSave = facultyMapper.toEntity(facultyDto);
        toSave.setCode(code);

        Faculty updated = facultyServ.save(toSave);
        return ResponseEntity.ok(facultyMapper.toDto(updated));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        facultyServ.deleteByCode(code);
        return ResponseEntity.noContent().build();
    }
}

/*
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


*/
