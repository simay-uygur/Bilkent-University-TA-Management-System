package com.example.repo.RequestRepos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.ProctorTaInDepartment;

@Repository
public interface ProctorTaInDepartmentRepo extends JpaRepository<ProctorTaInDepartment, Long>{
    boolean existsBySender_IdAndReceiver_NameAndExam_ExamIdAndIsRejected(
        Long senderId,
        String receiverName,
        Integer examId,
        Boolean isRejected
    );

    /**
     * Example “pending” finder:
     *   find all requests for a given instructor (sender) that are still pending
     */
    List<ProctorTaInDepartment> findAllBySender_IdAndIsPendingTrue(
        Long senderId
    );
}
