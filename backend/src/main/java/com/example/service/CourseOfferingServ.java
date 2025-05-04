// com/example/service/CourseOfferingService.java
package com.example.service;

import com.example.entity.Courses.CourseOffering;
import java.util.List;
import java.util.Optional;

public interface CourseOfferingServ {
    CourseOffering create(CourseOffering offering);
    CourseOffering update(Long id, CourseOffering offering);
    CourseOffering getById(Long id);
    List<CourseOffering> getAll();
    void delete(Long id);
    Optional<CourseOffering> getByCourseAndSemester(Long courseId, Long semesterId);

    boolean assignTA(Long taId, String courseCode);
}



