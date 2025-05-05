package com.example.repo;

import java.util.List;
import java.util.Optional;

import com.example.dto.StudentDto;
import com.example.dto.TaDto;
import com.example.dto.TaMiniDto;
import com.example.entity.Actors.TA;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Courses.Section;
import org.springframework.web.bind.annotation.*;

@Repository
public interface SectionRepo extends JpaRepository<Section, Integer>{

    @Query("""
    SELECT new com.example.dto.StudentDto(
        s.studentId,
        s.studentName,
        s.studentSurname,
        s.webmail,
        s.academicStatus,
        s.department,
        s.isActive,
        s.isGraduated
    )
    FROM Section sec
      JOIN sec.registeredStudents s
    WHERE sec.sectionId = :sectionId
""")
    List<StudentDto> findStudentDTOsBySectionId(@Param("sectionId") Long sectionId);

    //boolean existsBySectionId(int sectionId);

    boolean existsBySectionCodeEqualsIgnoreCase(String sectionCode);






    Optional<Section> findSectionByOffering_IdAndSectionCodeIgnoreCase(int offeringId, String sectionCode);
//  @Query("""
//    SELECT new com.example.dto.TaDto(
//      t.id,
//      t.name,
//      t.surname
//    )
//    FROM Section sec
//      JOIN sec.taAsStudents t
//    WHERE sec.sectionId = :sectionId
//    """)
//  List<TaDto> findTaDTOsBySectionId(@Param("sectionId") int sectionId);


//
//    @Query("""
//   SELECT NEW com.example.dto.TaMiniDto(
//            t.id,
//            t.name,
//            t.surname
//   )
//   FROM   Section sec
//          JOIN sec.taAsStudents t
//   WHERE  sec.sectionId = :sectionId
//""")
//    List<TaMiniDto> findTaDTOsBySectionId(@Param("sectionId") int sectionId);


    @Query("""
           SELECT  t
           FROM    Section  sec
                   JOIN     sec.registeredTas t
           WHERE   sec.sectionId = :sectionId
           """)
    List<TA> findTasBySectionId(@Param("sectionId") int sectionId);

    Optional<Section>findBySectionCodeIgnoreCase(String secCode);

    @Query("""
           SELECT  sec
           FROM    Section  sec
           WHERE   sec.sectionId = :sectionId
           """)
    Optional<List<Section>> findSectionsBySectionCodeIgnoreCase(String secCode);
}
