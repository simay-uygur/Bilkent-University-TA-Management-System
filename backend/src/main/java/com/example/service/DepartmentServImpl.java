package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.entity.Courses.Department;
import com.example.repo.DepartmentRepo;

public class DepartmentServImpl implements DepartmentServ{

    @Autowired
    private DepartmentRepo depRepo;

    @Override
    public boolean createDepartment(Department dep) {
        depRepo.saveAndFlush(dep);
        return true ;
    }
    
}
