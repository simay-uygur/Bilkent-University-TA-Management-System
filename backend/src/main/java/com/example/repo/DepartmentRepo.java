package com.example.repo;

import com.example.entity.Courses.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepo extends JpaRepository<Department, String> {
    // No extra methods needed for now because we only need existsById
    Optional<Department> findDepartmentByName(String name); //hope it works
}