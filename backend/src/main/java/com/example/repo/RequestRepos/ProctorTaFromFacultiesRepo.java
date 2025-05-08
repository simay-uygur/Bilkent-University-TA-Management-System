package com.example.repo.RequestRepos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.ProctorTaFromFaculties;

@Repository
public interface ProctorTaFromFacultiesRepo extends JpaRepository<ProctorTaFromFaculties, Long>{
    boolean existsByExamExamIdAndSenderId(Long senderId, int examId);
    boolean existsBySenderIdAndExamExamIdAndIsRejected(Long id, int examId, boolean isRejected);
    List<ProctorTaFromFaculties> findAllBySenderIdAndIsPendingTrue(Long id);
}
