package com.example.repo.RequestRepos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.ProctorTaInFaculty;

@Repository
public interface ProctorTaInFacultyRepo extends JpaRepository<ProctorTaInFaculty, Integer>{
    List<ProctorTaInFaculty> findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(Long receiverId);
}
