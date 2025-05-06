package com.example.repo.RequestRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.Swap;

@Repository
public interface SwapRepo extends JpaRepository<Swap, Long>{
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    boolean existsBySenderIdAndReceiverIdAndExamExamIdAndIsRejectedFalse(Long id, Long receiverId, int examId);
}
