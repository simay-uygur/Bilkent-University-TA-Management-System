package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;


@Repository
public interface ExamRepo extends JpaRepository<Exam, Integer>{
    Optional<Exam> findByExamId(int examId);
    @Query("""
        select  e
        from    Exam e
        join    e.assignedTas t
        where   t.id = :taId
        order by e.duration.start.year,
                 e.duration.start.month,
                 e.duration.start.day,
                 e.duration.start.hour,
                 e.duration.start.minute
        """)
    List<Exam> findAllByTaId(@Param("taId") Long taId);

    @Query("""
        SELECT e.examId
        FROM Exam e
        WHERE e.duration.start  <= :to
        AND e.duration.finish >= :from
    """)
    List<Integer> findOverlappingExamIds(
        @Param("from") Date from,
        @Param("to")   Date to
    );
}
