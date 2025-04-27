package com.example.service;

import com.example.repo.DepartmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepartmentServ {

    private final DepartmentRepo departmentRepo;

    public boolean departmentExists(String name) {
        return departmentRepo.existsById(name);
    }
}