package com.example.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entity.Actors.TA_DTO;
import com.example.entity.Courses.Section;
import com.example.entity.General.Student_DTO;

public interface SectionRepo extends JpaRepository<Section, Integer>{
    @Query("""
    SELECT new com.example.entity.General.Student_DTO(
      s.student_id,
      s.student_name,
      s.student_surname
    )
    FROM Section sec
      JOIN sec.students s
    WHERE sec.section_id = :sectionId
    """)
  List<Student_DTO> findStudentDTOsBySectionId(@Param("sectionId") int sectionId);

  @Query("""
    SELECT new com.example.entity.Actors.TA_DTO(
      t.id,
      t.name,
      t.surname
    )
    FROM Section sec
      JOIN sec.ta_as_students t
    WHERE sec.section_id = :sectionId
    """)
  List<TA_DTO> findTaDTOsBySectionId(@Param("sectionId") int sectionId);
}
