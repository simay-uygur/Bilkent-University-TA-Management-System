package com.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.General.ClassRoom;

@Repository
public interface ClassRoomRepo extends JpaRepository<ClassRoom, String>{
    Optional<ClassRoom> findByClassroomId(String classroomCode);
}
