package com.example.repo.RequestRepos;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.example.entity.General.Date;
import com.example.entity.Requests.RequestType;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.TransferProctoring;

import jakarta.transaction.Transactional;

@Repository
public interface TransferProctoringRepo extends JpaRepository<TransferProctoring, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    boolean existsBySenderIdAndReceiverIdAndExamExamIdAndIsRejected(Long id, Long recId, int examId, boolean isRejected);
    List<TransferProctoring> findAllByReceiverIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
        Long receiverId,
        Date from,
        Date to,
        Collection<RequestType> types
    );
    List<TransferProctoring> findAllBySenderIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
        Long receiverId,
        Date from,
        Date to,
        Collection<RequestType> types
    );

    @Modifying
    @Transactional
    int deleteByReceiverIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
        Long receiverId,
        Date start,
        Date end,
        Collection<RequestType> types
    );

    /**
     * Same for swaps *sent* by the user.
     */
    @Modifying
    @Transactional
    int deleteBySenderIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
        Long senderId,
        Date start,
        Date end,
        Collection<RequestType> types
    );
}
