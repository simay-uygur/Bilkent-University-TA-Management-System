package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.General.Date;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;

import jakarta.transaction.Transactional;

@Repository
public interface TaTaskRepo extends JpaRepository<TaTask, Integer> {

    /**
     * Find a single TaTask by the task’s ID and the TA’s ID.
     */
    @Query("""
      SELECT t
        FROM TaTask t
       WHERE t.task.taskId = :taskId
         AND t.taOwner.id  = :taId
    """)
    Optional<TaTask> findByTaskIdAndTaId(
        @Param("taskId") int    taskId,
        @Param("taId")   Long   taId
    );

    @Query("""
      SELECT t
        FROM TaTask t
       WHERE t.taOwner.id = :taId
    """)
    List<TaTask> findAllByTaId(@Param("taId") Long taId);

    @Query("""
      SELECT t
        FROM TaTask t
       WHERE t.taOwner.id  = :taId
         AND t.task.status  = 'PENDING'
    """)
    List<TaTask> findAllPendingTasksByTaId(@Param("taId") Long taId);

    @Query("""
      SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END
        FROM TaTask t
        WHERE t.task.taskId   = :taskId
        AND t.taOwner.id    = :taId""")
    boolean exists(int taskId, Long taId);

     /**
     * Returns true if the given TA has any proctoring assignment
     * whose start date lies between (inclusive) the two Date values.
     */@Query("""
        SELECT CASE WHEN COUNT(tt)>0 THEN TRUE ELSE FALSE END
        FROM TaTask tt
        WHERE tt.taOwner.id = :taId
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

      /*@Query("""
        SELECT CASE WHEN COUNT(tt)>0 THEN TRUE ELSE FALSE END
          FROM TaTask tt
        WHERE tt.taOwner.id     = :taId
          AND tt.task.exam IS NOT NULL
          AND tt.task.duration.start <= :to
          AND tt.task.duration.finish >= :from
      """)
      boolean existsByTaIdAndExamOverlap(
        @Param("taId") Long taId,
        @Param("from") Date  from,
        @Param("to")   Date  to
      );*/

      @Query("""
        SELECT t
          FROM TaTask tt
          JOIN tt.task t
         WHERE tt.taOwner.id       = :taId
           AND t.duration.start    <= :to
           AND t.duration.finish   >= :from
      """)
      List<Task> findTasksForTaInInterval(
          @Param("taId") Long taId,
          @Param("from") Date   from,
          @Param("to")   Date   to
      );

      @Modifying
      @Transactional
      @Query("""
        DELETE FROM TaTask tt
        WHERE tt.taOwner.id              = :taId
          AND tt.task.duration.start     <= :to
          AND tt.task.duration.finish    >= :from
      """)
      int deleteTaTasksForTaInInterval(
          @Param("taId") Long taId,
          @Param("from") Date   from,
          @Param("to")   Date   to
      );

      @Modifying
      @Query("DELETE FROM TaTask t WHERE t.task.taskId = :taskId AND t.taOwner.id = :taId")
      void deleteByTaskAndTa(@Param("taskId") int taskId,
                            @Param("taId")   Long  taId);
      void deleteAllByTaskTaskId(Long taskId);
}
