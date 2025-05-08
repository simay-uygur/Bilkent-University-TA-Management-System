package com.example.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.ProctoringDto;
import com.example.service.ProctoringServ;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/course/{course_code}/")
@RequiredArgsConstructor
public class ProctoringController {

    private final ProctoringServ proctoringServ;

    @GetMapping("proctoring/exam/{exam_id}")
    public CompletableFuture<ResponseEntity<List<ProctoringDto>>> getMethodName(@PathVariable String course_code, @PathVariable Integer exam_id) {
        return proctoringServ.getProctoringInfo(exam_id, course_code).thenApply(availableTas -> {
            if (availableTas != null) {
                return ResponseEntity.ok(availableTas);
            } else {
                return ResponseEntity.notFound().build();
            }
        });
    }
    
}
