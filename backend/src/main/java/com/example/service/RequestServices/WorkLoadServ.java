package com.example.service.RequestServices;

import java.util.List;

import com.example.entity.Requests.WorkLoadDto;

public interface WorkLoadServ {
    public void createWorkLoad(WorkLoadDto workLoadDto, Long senderId) ;
    public void updateWorkLoad(WorkLoadDto workLoadDto, Long senderId) ;
    public void deleteWorkLoad(Long id, Long senderId) ;
    public WorkLoadDto getWorkLoadById(Long id, Long senderId) ;
    public List<WorkLoadDto> getAllWorkLoadBySenderId(Long senderId) ;
    public List<WorkLoadDto> getAllWorkLoadByReceiverId(Long receiverId) ;
    public List<WorkLoadDto> getAllWorkLoadBySenderIdAndReceiverId(Long senderId, Long receiverId) ;
}
