package com.example.service.RequestServices;

import com.example.entity.Requests.ProctorTaInFacultyDto;

public interface ProctorTaInFacultyServ {
    public void createProctorTaInFacultyRequest(ProctorTaInFacultyDto dto, Long senderId);
    public void approveProctorTaInFacultyRequest(Long requestId, Long approverId);
    public void rejectProctorTaInFacultyRequest(Long requestId, Long approverId);
    public void cancelProctorTaInFacultyRequest(Long requestId, Long senderId);
    public void getProctorTaInFacultyRequestById(Long requestId, Long userId);
    public void getProctorTaInFacultyRequestBySenderId(Long senderId, Long userId);
    public void updateProctorTaInFacultyRequest(Long requestId, Long senderId, ProctorTaInFacultyDto dto);
}
