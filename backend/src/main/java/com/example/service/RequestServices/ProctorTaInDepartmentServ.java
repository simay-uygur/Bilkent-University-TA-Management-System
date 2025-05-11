package com.example.service.RequestServices;

import com.example.entity.Requests.ProctorTaInDepartmentDto;

public interface ProctorTaInDepartmentServ {
    public void createProctorTaInDepartmentRequest(ProctorTaInDepartmentDto dto, Long senderId);
    public void approveProctorTaInDepartmentRequest(Long requestId, String approverId);
    public void rejectProctorTaInDepartmentRequest(Long requestId, String approverId);
    public void cancelProctorTaInDepartmentRequest(Long requestId, String senderId);
    public void getProctorTaInDepartmentRequestById(Long requestId, Long userId);
    public void getProctorTaInDepartmentRequestBySenderId(Long senderId, Long userId);
    public void updateProctorTaInDepartmentRequest(Long requestId, Long senderId, ProctorTaInDepartmentDto dto);
}
