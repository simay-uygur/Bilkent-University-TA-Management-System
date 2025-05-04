package com.example.repo.RequestRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.SwapEnable;

@Repository
public interface SwapEnableRepo extends JpaRepository<SwapEnable, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    boolean existsBySenderIdAndReceiverIdAndExamExamIdAndIsRejected(Long id, Long receiverId, int examId, boolean isRejected);
}
