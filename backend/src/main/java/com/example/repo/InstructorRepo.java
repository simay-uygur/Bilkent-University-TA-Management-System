package com.example.repo;

import com.example.entity.Actors.Instructor;
import com.example.entity.Courses.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepo extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findById(Long id);
    // Spring-Data method in InstructorRepo
    List<Instructor> findByCoursesCourseCode(String courseCode);

    // JPQL to list sections for a given instructor
    @Query("select s from Section s where s.instructor.id = :instId")
    List<Section> findSectionsByInstructor(@Param("instId") Long instructorId);

    @Query("SELECT i FROM Instructor i WHERE i.department.name = :departmentName")
    Optional<List<Instructor>> findByDepartmentName(String departmentName);


}