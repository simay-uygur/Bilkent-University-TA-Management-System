package com.example.service;

import com.example.entity.Courses.Department;
import com.example.repo.DepartmentRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class DepartmentServImpl implements DepartmentServ {

    private final DepartmentRepo departmentRepo;
    private final LogService log;
    @Override
    public List<Department> getAllDepartments() {
        return departmentRepo.findAll();
    }

    @Override
    public boolean createDepartment(Department department) {
        departmentRepo.save(department);
        log.info("Deparment creation", "New Department with name: " +department.getName()+ " is created in the system");
        return true;
    }

    @Override
    public boolean departmentExists(String name) {
        return departmentRepo.existsById(name);
    }
}
