// com/example/service/CourseOfferingService.java
package com.example.service;

import java.io.IOException;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.example.dto.CourseOfferingDto;
import com.example.dto.EventDto;
import com.example.dto.ExamDto;
import com.example.dto.ExamSlotInfoDto;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Section;
import com.example.entity.General.Term;
import com.example.exception.GeneralExc;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    @Async("setExecutor")
    CompletableFuture<Boolean> createExamWithClassRoomGiven(ExamDto dto, String courseCode);

    CompletableFuture<Boolean> addTAs(String courseCode, Integer examId, List<Long> tas) throws GeneralExc; // Assuming you have an ExamDto class
    Section getSectionByNumber(String courseCode, int sectionNumber) ;

    //exam import function
    Map<String,Object> importExamsFromExcel(MultipartFile file) throws IOException;

    CompletableFuture<ExamSlotInfoDto> getExamSlotInfo(String courseCode, EventDto duration);
    boolean save(CourseOffering off);
    Term determineTerm(Month month);
}



