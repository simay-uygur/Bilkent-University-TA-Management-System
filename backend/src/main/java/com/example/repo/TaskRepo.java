package com.example.repo;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskState;

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

    List<Task> findByStatusNotIn(Collection<TaskState> states);
}
