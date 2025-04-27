package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Courses.Department;
import com.example.repo.DepartmentRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class DepartmentServImpl implements DepartmentServ{

    @Autowired
    private DepartmentRepo depRepo;

    @Override
    public boolean createDepartment(Department dep) {
        depRepo.saveAndFlush(dep);
        return true ;
    }
    
    @Override
    public boolean departmentExists(String name) {
        return depRepo.existsById(name);
    }
}
