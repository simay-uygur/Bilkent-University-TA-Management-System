package com.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Exams.Exam;

public interface ExamRepo extends JpaRepository<Exam, Integer>{
    
}
