package com.example.service;

import com.example.dto.ExamDto;
import com.example.dto.ExamExportDto;
import com.example.entity.Exams.Exam;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface ExamServ {
    public ExamDto getExam(Exam exam);
    public boolean createExam(Exam exam);
    //for uploading exam with exam rooms, time etc
    Map<String, Object> importExamsFromExcel(MultipartFile file) throws IOException;

    byte[] exportExamToPdf(String facultyCode, Integer examId) throws IOException;


    byte[] exportExamToPdfOnlyId(Integer examId) throws IOException;

    List<ExamDto> getExamsByCourseCode(String courseCode);
    public ExamDto getExamsIdByCourseCode(String courseCode, Integer examId);
}
