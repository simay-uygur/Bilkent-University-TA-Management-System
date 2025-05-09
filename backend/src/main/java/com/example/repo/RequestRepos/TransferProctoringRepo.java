package com.example.repo.RequestRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.General.Date;
import com.example.entity.Requests.TransferProctoring;

import jakarta.transaction.Transactional;

@Repository
public interface TransferProctoringRepo extends JpaRepository<TransferProctoring, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    boolean existsBySenderIdAndReceiverIdAndExamExamIdAndIsRejected(Long id, Long recId, int examId, boolean isRejected);
    @Modifying
    @Transactional
    @Query("""
      DELETE FROM TransferProctoring tp
       WHERE tp.receiver.id = :taId
         AND tp.isPending = true
         AND tp.exam.duration.start <= :to
         AND tp.exam.duration.finish >= :from
    """)
    int deleteReceivedForTaInInterval(
        @Param("taId") Long taId,
        @Param("from") Date   from,
        @Param("to")   Date   to
    );

    /**
     * Delete all pending TransferProctoring requests that a TA sent
     * for exams whose duration overlaps [from .. to].
     */
    @Modifying
    @Transactional
    @Query("""
      DELETE FROM TransferProctoring tp
       WHERE tp.sender.id = :taId
         AND tp.isPending = true
         AND tp.exam.duration.start <= :to
         AND tp.exam.duration.finish >= :from
    """)
    int deleteSentByTaInInterval(
        @Param("taId") Long taId,
        @Param("from") Date   from,
        @Param("to")   Date   to
    );
}
