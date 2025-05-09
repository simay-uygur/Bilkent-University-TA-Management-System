package com.example.repo.RequestRepos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Requests.WorkLoad;

public interface WorkLoadRepo extends JpaRepository<WorkLoad, Long>{
    boolean existsBySenderIdAndTaskTaskIdAndIsRejected(Long id, int taskId, boolean isRejected);
    void deleteAllByTaskTaskId(Long taskId);
}
