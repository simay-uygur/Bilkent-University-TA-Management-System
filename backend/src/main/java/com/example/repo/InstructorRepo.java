package com.example.repo;

import com.example.entity.Actors.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InstructorRepo extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findById(Long id);
}