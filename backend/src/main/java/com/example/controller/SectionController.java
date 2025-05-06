package com.example.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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

import com.example.dto.SectionDto;
import com.example.dto.TaskDto;
import com.example.entity.Courses.Section;
import com.example.mapper.SectionMapper;
import com.example.repo.SectionRepo;
import com.example.service.ExamServ;
import com.example.service.SectionServ;
import com.example.service.TaskServ;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionServ    sectionServ;
    private final SectionMapper  sectionMapper;
    private final SectionRepo    sectionRepo;
    private final ExamServ       examServ;
    private final TaskServ       taskServ;

    @GetMapping("/{id}")
    public SectionDto getById(@PathVariable int id) {
        Section sec = sectionServ.getById(id);
        return sectionMapper.toDto(sec);
    }
    @GetMapping("/sectionCode/{sectionCode}")
    public SectionDto getBySectionCode(@PathVariable String sectionCode) {
        Section sec = sectionServ.getBySectionCode(sectionCode);
        return sectionMapper.toDto(sec);
    }
    

    @GetMapping
    public List<SectionDto> getAll() {
        return sectionServ.getAll().stream()
                .map(sectionMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public Section createSection(@RequestBody Section section) {
        return sectionServ.create(section);
    }
   @PutMapping("/{id}")
    public Section updateSection(@PathVariable int id,
                                 @RequestBody Section section) {
        return sectionServ.update(id, section);
    }

    @DeleteMapping("/{id}")
    public void deleteSection(@PathVariable int id) {
        sectionServ.delete(id);
    } 

    @PostMapping("/{section_code}/task")
    public ResponseEntity<Boolean> createTask(@RequestBody TaskDto entity, @PathVariable String section_code) {
        return new ResponseEntity<>(taskServ.createTask(entity, section_code), HttpStatus.CREATED);
    }
}