package com.example.repo;


import java.util.HashSet;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskState;

import jakarta.transaction.Transactional;

@Repository
public interface TaskRepo extends JpaRepository<Task, Integer>{
    // Find all APPROVED tasks
    @Query("SELECT t FROM Task t WHERE t.status = 'APPROVED'")
    HashSet<Task> findApprovedTasks();

    // Find all PENDING tasks 
    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING'")
    List<Task> findPendingTasks();

    // Find all REJECTED tasks
    @Query("SELECT t FROM Task t WHERE t.status = 'REJECTED'")
    HashSet<Task> findRejectedTasks();

    // Find all DELETED tasks 
    @Query("SELECT t FROM Task t WHERE t.status = 'DELETED'")
    HashSet<Task> findDeletedTasks();

    @Modifying
    @Transactional
    @Query("""
        UPDATE Task t
        SET t.state = :completedState
        WHERE t.section.id = :sectionId
        AND t.taskType <> com.example.entity.Tasks.TaskType.Grading
    """)
    int markNonGradingTasksCompleted(
        @Param("sectionId") Long sectionId,
        @Param("completedState") TaskState completedState
    );
}
