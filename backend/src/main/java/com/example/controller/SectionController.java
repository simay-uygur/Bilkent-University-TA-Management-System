package com.example.controller;

import com.example.dto.SectionDto;
import com.example.dto.TaskDto;
import com.example.entity.Courses.Section;
import com.example.mapper.SectionMapper;
import com.example.service.SectionServ;
import com.example.service.TaskServ;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/sections")
public class SectionController {

    private final SectionServ    sectionServ;
    private final SectionMapper  sectionMapper;
    private final TaskServ      taskServ;

    public SectionController(SectionServ sectionServ,
                             SectionMapper sectionMapper, TaskServ taskServ) {
        this.taskServ      = taskServ;
        this.sectionServ   = sectionServ;
        this.sectionMapper = sectionMapper;
    }

    @GetMapping("/{id}")
    public SectionDto getById(@PathVariable int id) {
        Section sec = sectionServ.getById(id);
        return sectionMapper.toDto(sec);
    }
    @PostMapping("/{section_code}/task")
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto entity, @PathVariable String section_code) {
        return new ResponseEntity<>(taskServ.createTask(entity, section_code), HttpStatus.CREATED);
    }

    @GetMapping("/sectionCode/{sectionCode}")
    public SectionDto getBySectionCode(@PathVariable String sectionCode) {
        Section sec = sectionServ.getBySectionCode(sectionCode);
        return sectionMapper.toDto(sec);
    }
    /* @GetMapping("/department/{deptName}")
    public List<SectionDto> getByDepartment(@PathVariable String deptName) {
        return sectionServ.getByDepartment(deptName);
    } */

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

    //if this takes sectiondto update that  and also ta dto
    @PostMapping("/{sectionCode}/tas/{taId}")
    public CompletableFuture<ResponseEntity<Void>> assignTaToSection(
            @PathVariable String sectionCode,
            @PathVariable Long taId
    ) {
        return CompletableFuture.supplyAsync(() -> {
            sectionServ.assignTA(taId, sectionCode);
            return ResponseEntity.ok().build();
        });
    }
//
//    @PostMapping("/{sectionCode}/tasWithDtos/{taId}")
//    public CompletableFuture<ResponseEntity<Void>> assignTaToSectionWithDtos()

}