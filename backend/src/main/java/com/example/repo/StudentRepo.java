package com.example.repo;

import com.example.entity.General.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepo extends JpaRepository<Student, Long> {

    Optional<Student> findStudentByStudentId(Long studentId);
}

//not sure