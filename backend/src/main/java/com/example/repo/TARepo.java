package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
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
            "JOIN TaTask tt ON t.taskId = tt.task.taskId " +
            "WHERE tt.taOwner.id = :taId AND t.status = 'PENDING'")
    List<Task> findPendingTasksForTA(@Param("taId") Long taId); //it was giving an error


    @Query("SELECT t FROM TA t WHERE t.id = :id AND t.webmail = :webmail AND t.isDeleted = false")
    Optional<TA> findByIdAndWebmail(@Param("id") Long id, @Param("webmail") String webmail);

    @Query("SELECT t FROM TA t WHERE t.id = :id AND t.isDeleted = false")
    Optional<TA> findTAByTAId(long id);

    //Optional<TA> findTASByIdEqualsAndWebmailEquals(Long id, String webmail); - - böyle bir şey de varmış

    /**
     * Check if TA as a student has any exam (Task with an Exam) conflicting with the given range.
     */
    @Query("""
      SELECT CASE WHEN COUNT(tsk)>0 THEN TRUE ELSE FALSE END
      FROM Section s
      JOIN s.taAsStudents ta
      JOIN s.sectionTasksList tsk
      WHERE ta.id = :taId
        AND tsk.exam IS NOT NULL
        AND (
             (tsk.duration.start.year > :endYear)
          OR (tsk.duration.start.year = :endYear AND tsk.duration.start.month > :endMonth)
          OR (tsk.duration.start.year = :endYear AND tsk.duration.start.month = :endMonth AND tsk.duration.start.day >= :endDay)
        )
        AND (
             (tsk.duration.start.year < :startYear)
          OR (tsk.duration.start.year = :startYear AND tsk.duration.start.month < :startMonth)
          OR (tsk.duration.start.year = :startYear AND tsk.duration.start.month = :startMonth AND tsk.duration.start.day <= :startDay)
        )
    """)
    boolean existsExamConflictAsStudent(
        @Param("taId") Long taId,
        @Param("startYear") int startYear,
        @Param("startMonth") int startMonth,
        @Param("startDay") int startDay,
        @Param("endYear") int endYear,
        @Param("endMonth") int endMonth,
        @Param("endDay") int endDay
    );

    /**
     * Check if TA as a student has any lesson conflicting with the given date range.
     */
    @Query("""
      SELECT CASE WHEN COUNT(les)>0 THEN TRUE ELSE FALSE END
      FROM Lesson les
      JOIN les.section sec
      JOIN sec.taAsStudents ta
      WHERE ta.id = :taId
        AND (
             (les.duration.start.year > :endYear)
          OR (les.duration.start.year = :endYear AND les.duration.start.month > :endMonth)
          OR (les.duration.start.year = :endYear AND les.duration.start.month = :endMonth AND les.duration.start.day >= :endDay)
        )
        AND (
             (les.duration.finish.year < :startYear)
          OR (les.duration.finish.year = :startYear AND les.duration.finish.month < :startMonth)
          OR (les.duration.finish.year = :startYear AND les.duration.finish.month = :startMonth AND les.duration.finish.day <= :startDay)
        )
    """)
    boolean existsLessonConflictAsStudent(
        @Param("taId") Long taId,
        @Param("startYear") int startYear,
        @Param("startMonth") int startMonth,
        @Param("startDay") int startDay,
        @Param("endYear") int endYear,
        @Param("endMonth") int endMonth,
        @Param("endDay") int endDay
    );

    @Query("""
      SELECT ta
        FROM TA ta
      WHERE ta.isActive = TRUE
        AND NOT EXISTS (
          SELECT l
            FROM Lesson l
            JOIN l.section s
            JOIN s.taAsStudents tas
            WHERE tas       = ta
              AND l.duration.start  <= :to
              AND l.duration.finish >= :from
        )
        AND NOT EXISTS (
          SELECT tt
            FROM TaTask tt
            WHERE tt.taOwner = ta
              AND tt.task.duration.start  <= :to
              AND tt.task.duration.finish >= :from
        )
    """)
    List<TA> findAllFreeBetween(
      @Param("from") Date from,
      @Param("to")   Date to
    );


    @Query("""
       SELECT DISTINCT t
       FROM   Section  sec
              JOIN     sec.taAsStudents t
              LEFT JOIN FETCH t.courses
              LEFT JOIN FETCH t.tasOwnLessons
       WHERE  sec.sectionId = :sectionId
       """)
    List<TA> findTasWithAllRelations(@Param("sectionId") int sectionId);


    @Query("""
       SELECT DISTINCT t
       FROM   TA  t
              LEFT JOIN FETCH t.courses
              LEFT JOIN FETCH t.tasOwnLessons
       WHERE  t.department.name = :deptName
            AND t.isActive = TRUE
            AND t.isDeleted = FALSE
       """)
    List<TA> findByDepartment(String deptName);
}