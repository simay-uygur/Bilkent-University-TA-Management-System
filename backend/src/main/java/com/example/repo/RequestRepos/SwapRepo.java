package com.example.repo.RequestRepos;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.General.Date;
import com.example.entity.Requests.RequestType;
import com.example.entity.Requests.Swap;

import jakarta.transaction.Transactional;

@Repository
public interface SwapRepo extends JpaRepository<Swap, Long>{
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    boolean existsBySender_IdAndReceiver_IdAndSendersExam_ExamIdAndReceiversExam_ExamIdAndIsRejectedFalse(
            Long    senderId,
            Long    receiverId,
            Integer senderExamId,
            Integer receiverExamId);
    List<Swap> findAllByReceiverIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
        Long receiverId,
        Date from,
        Date to,
        Collection<RequestType> types
    );
    List<Swap> findAllBySenderIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
        Long receiverId,
        Date from,
        Date to,
        Collection<RequestType> types
    );

    @Modifying
    @Transactional
    @Query("""
      DELETE FROM Swap s
       WHERE s.receiver.id = :taId
         AND s.isPending = true
         AND s.exam.duration.start <= :to
         AND s.exam.duration.finish >= :from
    """)
    int deleteReceivedForTaInInterval(
        @Param("taId")   Long taId,
        @Param("from")   Date from,
        @Param("to")     Date to
    );

    /**
     * Delete all pending SwapRequests that a TA sent
     * for exams whose duration overlaps [from .. to].
     */
    @Modifying
    @Transactional
    @Query("""
      DELETE FROM Swap s
       WHERE s.sender.id = :taId
         AND s.isPending = true
         AND s.exam.duration.start <= :to
         AND s.exam.duration.finish >= :from
    """)
    int deleteSentByTaInInterval(
        @Param("taId")   Long taId,
        @Param("from")   Date from,
        @Param("to")     Date to
    );
}
