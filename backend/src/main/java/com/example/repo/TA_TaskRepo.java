package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Tasks.TA_Task;

@Repository
public interface TA_TaskRepo extends JpaRepository<TA_Task, Integer> {

    /**
     * Find a single TA_Task by the task’s ID and the TA’s ID.
     */
    @Query("""
      SELECT t
        FROM TA_Task t
       WHERE t.task.task_id = :taskId
         AND t.ta_owner.id  = :taId
    """)
    Optional<TA_Task> findByTaskIdAndTaId(
        @Param("taskId") int    taskId,
        @Param("taId")   Long   taId
    );

    @Query("""
      SELECT t
        FROM TA_Task t
       WHERE t.ta_owner.id = :taId
    """)
    List<TA_Task> findAllByTaId(@Param("taId") Long taId);

    @Query("""
      SELECT t
        FROM TA_Task t
       WHERE t.ta_owner.id  = :taId
         AND t.task.status  = 'PENDING'
    """)
    List<TA_Task> findAllPendingTasksByTaId(@Param("taId") Long taId);

    @Query("""
      SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END
        FROM TA_Task t
        WHERE t.task.task_id   = :taskId
        AND t.ta_owner.id    = :taId""")
    boolean exists(int taskId, Long taId);

}
