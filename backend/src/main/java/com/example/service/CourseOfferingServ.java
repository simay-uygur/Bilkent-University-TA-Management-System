// com/example/service/CourseOfferingService.java
package com.example.service;

import java.util.List;
import java.util.Optional;

import com.example.dto.ExamDto;
import com.example.entity.Courses.CourseOffering;

public interface CourseOfferingServ {
    CourseOffering create(CourseOffering offering);
    CourseOffering update(Long id, CourseOffering offering);
    CourseOffering getById(Long id);
    List<CourseOffering> getAll();
    void delete(Long id);
    Optional<CourseOffering> getByCourseAndSemester(Long courseId, Long semesterId);

    boolean assignTA(Long taId, String courseCode);

    CourseOffering getCurrentOffering(String courseCode);
    void createExam(ExamDto exam, String courseCode); // Assuming you have an ExamDto class
}



