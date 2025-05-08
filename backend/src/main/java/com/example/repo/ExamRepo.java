package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Exams.Exam;


@Repository
public interface ExamRepo extends JpaRepository<Exam, Integer>{
    Optional<Exam> findByExamId(int examId);
}
