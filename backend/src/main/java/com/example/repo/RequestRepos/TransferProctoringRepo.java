package com.example.repo.RequestRepos;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.TransferProctoring;

import jakarta.transaction.Transactional;

@Repository
public interface TransferProctoringRepo extends JpaRepository<TransferProctoring, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    boolean existsBySenderIdAndReceiverIdAndExamExamIdAndIsRejected(Long id, Long recId, int examId, boolean isRejected);
    
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
    DELETE FROM Swap s
    WHERE s.isPending = true
        AND ( s.sendersExam.examId   IN :examIds
            OR s.receiversExam.examId IN :examIds )
        AND ( s.sender.id   = :taId
            OR s.receiver.id = :taId )
    """)
    int deleteAllSwapsForTaAndExamIds(
        @Param("taId")    Long taId,
        @Param("examIds") Collection<Integer> examIds
    );
}
