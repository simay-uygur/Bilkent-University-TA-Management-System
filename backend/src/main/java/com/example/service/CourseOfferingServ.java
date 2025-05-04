// com/example/service/CourseOfferingService.java
package com.example.service;

import com.example.entity.Courses.CourseOffering;

import java.util.List;

import com.example.dto.ExamDto;

public interface CourseOfferingServ {
    CourseOffering create(CourseOffering offering);
    CourseOffering update(Long id, CourseOffering offering);
    CourseOffering getById(Long id);
    List<CourseOffering> getAll();
    void delete(Long id);
    CourseOffering getCurrentOffering(String courseCode);
    void createExam(ExamDto exam, String courseCode); // Assuming you have an ExamDto class
}



