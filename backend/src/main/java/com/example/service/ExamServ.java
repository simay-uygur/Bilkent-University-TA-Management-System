package com.example.service;

import com.example.dto.ExamDto;
import com.example.entity.Exams.Exam;


public interface ExamServ {
    public ExamDto getExam(Exam exam);
    public boolean createExam(Exam exam);
}