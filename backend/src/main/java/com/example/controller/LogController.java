package com.example.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.AppLog;
import com.example.repo.AppLogRepo;

import lombok.RequiredArgsConstructor;

/**
 * GET /api/logs?page=0&size=50 → son 50 log kaydını döner.
 * İstersen projeye ekle, istemezsen kaldır.
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final AppLogRepo repo;

    @GetMapping
    public ResponseEntity<List<AppLog>> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<AppLog> p = repo.findAll(
                PageRequest.of(page, size, Sort.by("id").descending()));
        return ResponseEntity.ok(p.getContent());
    }
}
