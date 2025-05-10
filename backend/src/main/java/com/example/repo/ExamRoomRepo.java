package com.example.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Exams.ExamRoom;

@Repository
public interface ExamRoomRepo extends JpaRepository<ExamRoom, Integer>{
    List<ExamRoom> findByAssignedTas_Id(Long taId);
}
