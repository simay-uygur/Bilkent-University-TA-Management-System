// com/example/service/CourseOfferingService.java
package com.example.service;

import com.example.dto.CourseDto;
import com.example.dto.CourseOfferingDto;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.CourseOffering;
import java.util.List;
import java.util.Optional;

public interface CourseOfferingServ {
    CourseOffering create(CourseOffering offering);
    CourseOffering update(Long id, CourseOffering offering);
    CourseOfferingDto getById(Long id);
    CourseOfferingDto getByCourseCode(String code);
    List<CourseOffering> getAll();
    void delete(Long id);
    Optional<CourseOffering> getByCourseAndSemester(Long courseId, Long semesterId);

    boolean assignTA(Long taId, String courseCode);
     public List<CourseOfferingDto> getOfferingsByDepartment(String deptName);
}



