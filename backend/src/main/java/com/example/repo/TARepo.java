package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Actors.TA;
import com.example.entity.Tasks.Task;

@Repository
public interface TARepo extends JpaRepository<TA, Long> { // TA is the entity and Long is the type of the primary key
    @Query("SELECT t FROM TA t WHERE t.isDeleted = false")
    List<TA> findAllTAs(); // fixed query

    /*@Query("SELECT pt FROM PublicTask pt " +
           "JOIN pt.tas_list tas " +
           "WHERE tas.id = :taId AND pt.status = 'PENDING'")
    List<PublicTask> findPendingPublicTasksForTA(@Param("taId") Long taId);

    @Query("SELECT pt FROM PrivateTask pt " +
           "WHERE pt.ta_owner.id = :taId AND pt.status = 'PENDING'")
    List<PrivateTask> findPendingPrivateTasksForTA(@Param("taId") Long taId);*/
    
    // This query will fetch all tasks (both public and private) for a given TA that are pending
    @Query("SELECT t FROM Task t " +
            "JOIN TA_Task tt ON t.task_id = tt.task.task_id " +
            "WHERE tt.ta_owner.id = :taId AND t.status = 'PENDING'")
    List<Task> findPendingTasksForTA(@Param("taId") Long taId); //it was giving an error


    @Query("SELECT t FROM TA t WHERE t.id = :id AND t.webmail = :webmail AND t.isDeleted = false")
    Optional<TA> findByIdAndWebmail(@Param("id") Long id, @Param("webmail") String webmail);
}