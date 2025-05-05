package com.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.General.ClassRoom;

import java.util.Optional;

@Repository
public interface ClassRoomRepo extends JpaRepository<ClassRoom, String> {
    boolean existsByClassroomId(String id);

    Optional<ClassRoom> findClassRoomByClassroomId(String classroomId);
    Optional<ClassRoom> findClassRoomByClassroomIdEqualsIgnoreCase(String classroomId);
}
