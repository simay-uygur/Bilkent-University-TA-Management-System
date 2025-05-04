package com.example.controller;

import com.example.dto.InstructorDto;
import com.example.entity.Actors.Instructor;
import com.example.mapper.InstructorMapper;
import com.example.service.InstructorServ;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorServ instructorServ;
    private final InstructorMapper instructorMapper;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadInstructors(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(instructorServ.importInstructorsFromExcel(file));
    }

    @PostMapping
    public ResponseEntity<Instructor> createInstructor(@RequestBody Instructor instructor) {
        return ResponseEntity.ok(instructorServ.createInstructor(instructor));
    }
    @GetMapping("/{id}")
    public ResponseEntity<InstructorDto> getInstructorById(@PathVariable Long id) {
        return ResponseEntity.ok(instructorServ.getInstructorById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Instructor> updateInstructor(@PathVariable Long id, @RequestBody Instructor instructor){
        return ResponseEntity.ok(instructorServ.updateInstructor(id, instructor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInstructor(@PathVariable Long id) {
        instructorServ.deleteInstructor(id);
        return ResponseEntity.ok("Instructor deleted successfully.");
    }
//
//    @GetMapping()
//    public ResponseEntity<List<Instructor>> getAllInstructors() {
//        return ResponseEntity.ok(instructorServ.getAllInstructors());
//    }

    //this will be used to get all instructor dto's
    @GetMapping("/dto")
    public ResponseEntity<List<InstructorDto>> getAllInstructorDtos() {
        List<InstructorDto> dtos = instructorServ.getAllInstructors().stream()
                .map(instructorMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/v1/instructors
     * Returns all instructors as DTOs.
     */
    @GetMapping
    public ResponseEntity<List<InstructorDto>> getAllInstructors() {
        List<InstructorDto> dtos = instructorServ.getAllInstructorsDto();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/v1/instructors/department/{deptName}
     * Returns all instructors in the given department as DTOs.
     */
    @GetMapping("/department/{deptName}")
    public ResponseEntity<List<InstructorDto>> getByDepartment(
            @PathVariable("deptName") String deptName
    ) {
        List<InstructorDto> dtos = instructorServ.getInstructorsByDepartment(deptName);
        return ResponseEntity.ok(dtos);
    }

}