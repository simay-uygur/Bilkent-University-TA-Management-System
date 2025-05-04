package com.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.General.ClassRoom;

@Repository
public interface ClassRoomRepo extends JpaRepository<ClassRoom, String>{
    boolean existsByClassroomId(String id);
    Optional<ClassRoom> findByClassroomId(String classroomCode);
}
