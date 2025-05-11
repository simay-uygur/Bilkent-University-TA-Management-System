package com.example.service.RequestServices;

import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromFacultiesDto;

public interface ProctorTaFromFacultiesServ {
    public void createProctorTaFromFacultiesRequest(ProctorTaFromFacultiesDto dto, Long senderId);
    public void approveProctorTaFromFacultiesRequest(Long requestId, Long approverId);
    public void rejectProctorTaFromFacultiesRequest(Long requestId, Long approverId);
    public void cancelProctorTaFromFacultiesRequest(Long requestId, Long senderId);
    public ProctorTaFromFaculties getProctorTaFromFacultiesRequestById(Long requestId, Long userId);
    public void updateProctorTaFromFacultiesRequest(ProctorTaFromFacultiesDto dto, Long requestId, Long userId);
    public void deleteProctorTaFromFacultiesRequest(Long requestId, Long userId);
    //public List<ProctorTaFromFacultiesDto> getReceivedRequestsOfTheFaculty(Long deanOffice);
}
