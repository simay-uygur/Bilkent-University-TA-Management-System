package com.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Exams.Exam;

@Repository
public interface ExamRepo extends JpaRepository<Exam, Integer>{
    
}
