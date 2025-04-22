package com.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.General.ClassRoom;

public interface ClassRoomRepo extends JpaRepository<ClassRoom, Integer>{
    
}
