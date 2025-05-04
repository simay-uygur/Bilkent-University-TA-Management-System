
package com.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Courses.CourseOffering;
import com.example.entity.General.Term;

public interface CourseOfferingRepo extends JpaRepository<CourseOffering, Long> {

    //Optional<CourseOffering> findByCourseCodeAndSemesterYearAndSemesterTerm(String courseCode, int year, String term); //not used
    //Optional<CourseOffering> findCourseOfferingByCourse_CourseIdAndSemester_SemesterId(Long courseId, Long semesterId);

    Optional<CourseOffering> findByCourse_CourseIdAndSemester_Id(Long courseId, Long semesterId);

    //Optional<CourseOffering> findCourseOfferingByCourse_CourseIdAndSemester_SemesterId(Integer courseId, Long semesterId);
    //Optional<CourseOffering> findByCourse_IdAndSemester_Id(Integer courseId, Long semesterId);
    //Optional<CourseOffering> findByCourse_IdAndSemester_Id(Integer courseId, Long semesterId);

    Optional<CourseOffering> findByCourse_CourseCodeAndSemester_YearAndSemester_Term(
        String courseCode,
        int year,
        Term term
    );

    Optional<CourseOffering> findByCourse_CourseCode(String courseCode);
}
