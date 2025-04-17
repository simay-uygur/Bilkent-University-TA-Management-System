package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Tasks.TA_Task;
import com.example.entity.Tasks.TA_TaskId;

@Repository
public interface TA_TaskRepo extends JpaRepository<TA_Task, TA_TaskId> {

    // Custom query to find by composite ID
    @Query("SELECT tat FROM TA_Task tat WHERE tat.task.task_id = :task_id AND tat.ta_owner.id = :ta_id")
    Optional<TA_Task> findByTaskIdAndTaId(
        @Param("task_id") int task_id,
        @Param("ta_id") Long ta_id
    );

    // Alternative using the embedded ID directly
    Optional<TA_Task> findById(TA_TaskId id);

    // Query to get all tasks for a specific TA
    @Query("SELECT tat FROM TA_Task tat WHERE tat.ta_owner.id = :ta_id")
    List<TA_Task> findAllByTaId(@Param("ta_id") Long ta_id);

    // Query to get all pending tasks for a TA
    @Query("SELECT tat FROM TA_Task tat WHERE tat.ta_owner.id = :ta_id AND tat.task.status = 'PENDING'")
    List<TA_Task> findAllPendingTasksByTaId(@Param("ta_id") Long ta_id);
}