package com.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entity.Courses.Department;

public interface DepartmentRepo extends JpaRepository<Department, Integer>{
    @Query("""
    SELECT d
    FROM Deparment d
    WHERE d.dep_name = :depName
    """)
    Optional<Department> findByDepartmentName(@Param("depName") String depName);
}
