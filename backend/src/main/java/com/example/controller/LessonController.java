package com.example.controller;

import com.example.dto.LessonDto;
import com.example.service.LessonServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonServ lessonServ;

    // CREATE a new lesson
    @PostMapping()
    public ResponseEntity<List<LessonDto>> createLesson(@RequestBody LessonDto dto) {
        List<LessonDto> created = lessonServ.createLessonDtoList(dto);
        return ResponseEntity.ok(created);
    }

    // GET all lessons
    @GetMapping
    public ResponseEntity<List<LessonDto>> getAllLessons() {
        return ResponseEntity.ok(lessonServ.getAllLessons());
    }

    // GET lesson by ID
    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonServ.getLessonById(id));
    }

    // UPDATE lesson
    @PutMapping("/{id}")
    public ResponseEntity<LessonDto> updateLesson(@PathVariable Long id, @RequestBody LessonDto dto) {
        return ResponseEntity.ok(lessonServ.updateLesson(id, dto));
    }

    // DELETE lesson
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable Long id) {
        lessonServ.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

}