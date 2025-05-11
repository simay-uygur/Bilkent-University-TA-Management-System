package com.example.service.RequestServices;

import com.example.entity.Requests.ProctorTaInFacultyDto;

public interface ProctorTaInFacultyServ {
    ProctorTaInFacultyDto escalateToFaculty(Long depReqId);
    boolean approve(Long reqId, Long approverId);
    boolean reject(Long reqId, Long rejecterId);
}
