package com.example.repo.RequestRepos;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.entity.Requests.Leave;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface LeaveRepo extends JpaRepository<Leave, Long> {
    boolean existsByLeaveId(Long leaveId);
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    Optional<List<Leave>> findBySenderId(Long senderId);
    Optional<List<Leave>> findByReceiverId(Long receiverId);
    Optional<List<Leave>> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
}

