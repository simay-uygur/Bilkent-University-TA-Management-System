package com.example.service;

import com.example.entity.Exams.Exam;

public interface ProctoringServ {
    public void autoAssignment(Exam exam);
    public void manualAssignment(Exam exam, boolean[] restrictions);
}
