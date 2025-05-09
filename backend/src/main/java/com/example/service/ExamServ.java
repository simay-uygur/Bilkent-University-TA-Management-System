package com.example.service;

import com.example.dto.ExamDto;
import com.example.entity.Exams.Exam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


public interface ExamServ {
    public ExamDto getExam(Exam exam);
    public boolean createExam(Exam exam);

    //for uploading exam with exam rooms, time etc
    Map<String, Object> importExamsFromExcel(MultipartFile file) throws IOException;
}
