package com.example.service;

import com.example.entity.Exams.Exam;
import com.example.entity.Exams.Exam_DTO;

public interface ExamServ {
    public Exam_DTO getExam(Exam exam);
    public boolean createExam(Exam exam);
}
