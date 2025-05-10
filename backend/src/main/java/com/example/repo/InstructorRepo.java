package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Actors.Instructor;
import com.example.entity.Courses.Section;

@Repository
public interface InstructorRepo extends JpaRepository<Instructor, Long> {
    @Query("SELECT i FROM Instructor i WHERE i.id = :id")
    Optional<Instructor> findById(Long id);
    // Spring-Data method in InstructorRepo
    //List<Instructor> findByCoursesCourseCode(String courseCode);
    List<Instructor> findDistinctBySections_Offering_Course_CourseCode(String courseCode);

    // JPQL to list sections for a given instructor
    @Query("select s from Section s where s.instructor.id = :instId")
    List<Section> findSectionsByInstructor(@Param("instId") Long instructorId);

    Optional<List<Instructor>> findByDepartmentName(String departmentName);

    Optional<Instructor> findByIdAndDepartment_Faculty_Code(
            Long id,
            String facultyCode
    );



}