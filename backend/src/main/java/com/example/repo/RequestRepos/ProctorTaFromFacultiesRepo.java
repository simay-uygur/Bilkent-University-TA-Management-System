package com.example.repo.RequestRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.ProctorTaFromFaculties;

@Repository
public interface ProctorTaFromFacultiesRepo extends JpaRepository<ProctorTaFromFaculties, Long>{
    boolean existsByExamIdAndSenderId(Long senderId, int examId);
    boolean existsBySenderIdAndExamIdAndIsRejected(Long id, int examId, boolean isRejected);
}
