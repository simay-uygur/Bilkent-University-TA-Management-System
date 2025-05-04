package com.example.controller;

import com.example.dto.ClassRoomDto;
import com.example.dto.FailedRowInfo;
import com.example.entity.General.ClassRoom;
import com.example.service.ClassRoomServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
public class ClassRoomController {

    private final ClassRoomServ classRoomService;

    @PostMapping
    public ResponseEntity<ClassRoomDto> create(@RequestBody ClassRoomDto dto) {
        return ResponseEntity.ok(classRoomService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassRoomDto> update(@PathVariable String id, @RequestBody ClassRoomDto dto) {
        return ResponseEntity.ok(classRoomService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        classRoomService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassRoomDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(classRoomService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ClassRoomDto>> getAll() {
        return ResponseEntity.ok(classRoomService.getAll());
    }

}




