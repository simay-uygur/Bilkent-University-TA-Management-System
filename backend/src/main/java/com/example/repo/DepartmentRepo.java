package com.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.entity.Courses.Department;

@Repository
public interface DepartmentRepo extends JpaRepository<Department, String> {
    // No extra methods needed for now because we only need existsById
    @Query("""
    SELECT d
      FROM Department d
     WHERE d.name = :name
    """)
    Optional<Department> findDepartmentByName(String name); //hope it works
}