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
    @Query("SELECT tat FROM TA_Task tat WHERE tat.task.task_id = :taskId AND tat.ta.id = :taId")
    Optional<TA_Task> findByTaskIdAndTaId(
        @Param("taskId") int taskId,
        @Param("taId") Long taId
    );
    
    // Alternative using the embedded ID directly
    Optional<TA_Task> findById(TA_TaskId id);

    //Query to get all tas tasks in a list
    @Query("SELECT tat FROM TA_Task tat WHERE tat.ta.id = :taId")
    List<TA_Task> findAllByTaId(@Param("taId") Long taId);

    //Query to get all tasks of a specific type for a TA
    @Query("SELECT tat FROM TA_Task tat WHERE tat.ta.id = :taId AND tat.task.taskType = :taskType")
    List<TA_Task> findAllByTaIdAndTaskType(@Param("taId") Long taId, @Param("taskType") String taskType);

    //Query to get all tasks of a specific type for a TA and a specific date
    @Query("SELECT tat FROM TA_Task tat WHERE tat.ta.id = :taId AND tat.task.taskType = :taskType AND tat.task.startDate <= :date AND tat.task.endDate >= :date")
    List<TA_Task> findAllByTaIdAndTaskTypeAndDate(
        @Param("taId") Long taId,
        @Param("taskType") String taskType,
        @Param("date") String date
    );

    //Query to get all tasks of a specific type for a TA and a specific date range
    @Query("SELECT tat FROM TA_Task tat WHERE tat.ta.id = :taId AND tat.task.taskType = :taskType AND tat.task.startDate <= :endDate AND tat.task.endDate >= :startDate")
    List<TA_Task> findAllByTaIdAndTaskTypeAndDateRange(
        @Param("taId") Long taId,
        @Param("taskType") String taskType,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    //Query to get all pending tasks for a TA
    @Query("SELECT tat FROM TA_Task tat WHERE tat.ta.id = :taId AND tat.task.taskType = 'PENDING'")
    List<TA_Task> findAllPendingTasksByTaId(@Param("taId") Long taId);
}
