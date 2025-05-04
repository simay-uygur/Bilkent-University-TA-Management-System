package com.example.controller;

import com.example.dto.SectionDto;
import com.example.entity.Courses.Section;
import com.example.mapper.SectionMapper;
import com.example.service.SectionServ;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/sections")
public class SectionController {

    private final SectionServ    sectionServ;
    private final SectionMapper  sectionMapper;

    public SectionController(SectionServ sectionServ,
                             SectionMapper sectionMapper) {
        this.sectionServ   = sectionServ;
        this.sectionMapper = sectionMapper;
    }

    @GetMapping("/{id}")
    public SectionDto getById(@PathVariable int id) {
        Section sec = sectionServ.getById(id);
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
}