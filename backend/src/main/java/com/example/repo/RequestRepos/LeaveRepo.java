package com.example.repo.RequestRepos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.Leave;

@Repository
public interface LeaveRepo extends JpaRepository<Leave, Long> {
    Optional<List<Leave>> findBySenderId(Long senderId);
    List<Leave> findAllByReceiverNameAndIsPendingTrue(String id);
    Optional<List<Leave>> findByReceiverName(String name);
    boolean existsBySenderIdAndReceiverNameAndIsRejected(Long senderId, String receiverName, boolean isRejected);
    List<Leave> findByReceiver_NameAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(String depName);

}

