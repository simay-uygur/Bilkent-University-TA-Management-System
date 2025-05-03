package com.example.repo;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.General.Faculty;

@Repository
public interface FacultyRepo extends JpaRepository<Faculty, String> {
    Optional<Faculty> findByCode(String code);
}
