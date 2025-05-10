package com.example.repo;

import java.util.List;
import java.util.Optional;

import com.example.entity.Courses.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Courses.Course;
import com.example.entity.Tasks.Task;
@Repository
public interface CourseRepo extends JpaRepository<Course, Integer>{


    @Query("""
    SELECT c
      FROM Course c
     WHERE c.courseCode = :courseCode
    """)
    Optional<Course> findByCourseCode(@Param("courseCode") String courseCode);

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
          FROM Course c
         WHERE c.courseCode = :courseCode
    """)
    boolean existsByCourseCode(@Param("courseCode") String courseCode);

    @Query(value = "SELECT t.* FROM task_table t " +
    "JOIN course c ON t.course_id = c.course_id " +
    "WHERE t.task_id = :taskId AND c.course_code = :courseCode", 
    nativeQuery = true)
    Optional<Task> findTask(@Param("taskId") int taskId, 
                              @Param("courseCode") String courseCode);
                            
    @Query("""
    SELECT c
      FROM Course c
     WHERE c.courseCode = :courseCode
     """
    )    
    Optional<Course> findCourseByCourseCode(String courseCode);

    Optional<Course> findCourseByCourseCodeAndDepartmentName(String courseCode, String departmentName);

    boolean existsCourseByCourseCodeEquals(String courseCode);

    Optional<Course> findByCourseCodeIgnoreCase(String courseCode);

    @Query("""
    SELECT c
      FROM Course c
     WHERE c.department.name = :deptName
""")
    Optional<List<Course>> findCourseByDepartmentName(
            @Param("deptName") String deptName
    );

    List<Course> findByDepartment_Faculty_Code(String facultyCode);

    List<Course> findByDepartmentIn(List<Department> departments);
}
