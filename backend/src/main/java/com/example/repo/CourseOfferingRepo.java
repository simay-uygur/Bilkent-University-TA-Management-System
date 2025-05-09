
package com.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entity.Courses.CourseOffering;
import com.example.entity.General.Term;
import java.util.List;
import java.util.Optional;

public interface CourseOfferingRepo extends JpaRepository<CourseOffering, Long> {

    //Optional<CourseOffering> findByCourseCodeAndSemesterYearAndSemesterTerm(String courseCode, int year, String term); //not used
    //Optional<CourseOffering> findCourseOfferingByCourse_CourseIdAndSemester_SemesterId(Long courseId, Long semesterId);
    @Query("""

  SELECT co
    FROM CourseOffering co
   WHERE co.course.courseId = :courseId
     AND co.semester.id = :semesterId
""")
    Optional<CourseOffering> findByCourse_CourseIdAndSemester_Id(Long courseId, Long semesterId);


    @Query("""
  SELECT co
    FROM CourseOffering co
   WHERE co.course.department.name = :deptName
""")
Optional<List<CourseOffering>> findByCourseDepartmentName(
  @Param("deptName") String deptName
);
@Query("""
  SELECT co
    FROM CourseOffering co
   WHERE co.course.courseId = :courseId
""")
Optional<CourseOffering> findById(Long id);

@Query("""
  SELECT co
    FROM CourseOffering co
   WHERE co.course.courseCode = :code
""")  
Optional<List<CourseOffering>> findByCoursesCode(String code);

@Query("""
  SELECT co
    FROM CourseOffering co
   WHERE co.course.courseCode = :code
""")
Optional<CourseOffering>findByCourseCode(@Param("code") String code);



    //Optional<CourseOffering> findCourseOfferingByCourse_CourseIdAndSemester_SemesterId(Integer courseId, Long semesterId);
    //Optional<CourseOffering> findByCourse_IdAndSemester_Id(Integer courseId, Long semesterId);
    //Optional<CourseOffering> findByCourse_IdAndSemester_Id(Integer courseId, Long semesterId);

    Optional<CourseOffering> findByCourse_CourseCodeAndSemester_YearAndSemester_Term(
        String courseCode,
        int year,
        Term term
    );

    Optional<CourseOffering> findByCourse_CourseCode(String courseCode);

    @Query("""
  SELECT co
    FROM CourseOffering co
   WHERE co.semester.term = :term
     AND co.semester.year = :year
""")
    Optional<List<CourseOffering>> findBySemester_TermAndSemester_Year(String term, int year);
}
