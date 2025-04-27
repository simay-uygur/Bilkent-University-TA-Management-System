package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.General.Date;
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

     /**
     * Returns true if the given TA has any proctoring assignment
     * whose start date lies between (inclusive) the two Date values.
     */@Query("""
        SELECT CASE WHEN COUNT(tt)>0 THEN TRUE ELSE FALSE END
        FROM TA_Task tt
        WHERE tt.ta_owner.id = :taId
          AND (
              (tt.task.duration.start.year > :fromYear)
            OR (tt.task.duration.start.year = :fromYear
                AND tt.task.duration.start.month > :fromMonth)
            OR (tt.task.duration.start.year = :fromYear
                AND tt.task.duration.start.month = :fromMonth
                AND tt.task.duration.start.day >= :fromDay)
          )
          AND (
              (tt.task.duration.start.year < :toYear)
            OR (tt.task.duration.start.year = :toYear
                AND tt.task.duration.start.month < :toMonth)
            OR (tt.task.duration.start.year = :toYear
                AND tt.task.duration.start.month = :toMonth
                AND tt.task.duration.start.day <= :toDay)
          )
      """)
      boolean existsByTaAndDateBetween(
          @Param("taId")     Long taId,
          @Param("fromYear") int fromYear,
          @Param("fromMonth")int fromMonth,
          @Param("fromDay")  int fromDay,
          @Param("toYear")   int toYear,
          @Param("toMonth")  int toMonth,
          @Param("toDay")    int toDay
      );

      @Query("""
        SELECT CASE WHEN COUNT(tt)>0 THEN TRUE ELSE FALSE END
          FROM TA_Task tt
        WHERE tt.ta_owner.id     = :taId
          AND tt.task.exam IS NOT NULL
          AND tt.task.duration.start <= :to
          AND tt.task.duration.finish >= :from
      """)
      boolean existsByTaIdAndExamOverlap(
        @Param("taId") Long taId,
        @Param("from") Date  from,
        @Param("to")   Date  to
      );
}
