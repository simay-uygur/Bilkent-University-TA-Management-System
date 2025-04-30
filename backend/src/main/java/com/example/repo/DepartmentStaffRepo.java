package com.example.repo;

import com.example.entity.Actors.DepartmentStaff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentStaffRepo extends JpaRepository<DepartmentStaff, Long> {
}