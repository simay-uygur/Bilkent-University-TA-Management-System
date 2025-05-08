package com.example.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.dto.ExamDto;
import com.example.dto.ProctoringDto;

public interface ProctoringServ {
    public CompletableFuture<List<ProctoringDto>> getProctoringInfo(Integer examId, String courseCode);
}
