
package com.example.repo;

import com.example.entity.Courses.CourseOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
