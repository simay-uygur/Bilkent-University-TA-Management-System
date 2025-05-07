// com/example/service/CourseOfferingService.java
package com.example.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.example.dto.CourseOfferingDto;
import com.example.dto.ExamDto;
import com.example.entity.Courses.CourseOffering;
import com.example.exception.GeneralExc;

public interface CourseOfferingServ {
    CourseOffering create(CourseOffering offering);
    CourseOffering update(Long id, CourseOffering offering);
    CourseOfferingDto getById(Long id);
    List<CourseOfferingDto> getCoursesByCourseCode(String code);
    CourseOfferingDto getCourseByCourseCode(String code);
    List<CourseOffering> getAll();
    void delete(Long id);
    Optional<CourseOffering> getByCourseAndSemester(Long courseId, Long semesterId);

    // boolean assignTA(Long taId, String courseCode);
     public List<CourseOfferingDto> getOfferingsByDepartment(String deptName);

    CourseOffering getCurrentOffering(String courseCode);
    CompletableFuture<Boolean> createExam(ExamDto exam, String courseCode); // Assuming you have an ExamDto class
    CompletableFuture<Boolean> addTAs(String courseCode, Integer examId, List<Long> tas) throws GeneralExc; // Assuming you have an ExamDto class
}



