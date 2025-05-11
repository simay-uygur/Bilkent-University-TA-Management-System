package com.example.service;

import java.util.List;

import com.example.dto.RequestDto;
import com.example.dto.TaDto;
import com.example.entity.Actors.User;
import com.example.entity.General.Event;
import com.example.entity.Requests.Request;

public interface RequestServ {
    public List<Request> getAllRequests();
    public Request getRequestById(Long req_id);
    public List<RequestDto> getReceivedRequestsOfTheUser(Long userId);
    public boolean createRequest(Request req);
    public void checkLeaveRequests();
    public void deleteAllReceivedAndSendedSwapAndTransferRequestsBySomeTime(User u, Event duration);
    public List<RequestDto> getReceivedRequestsOfTheTa(Long taId);
    public List<RequestDto> getReceivedRequestsOfTheInstructor(Long taId);
    public List<RequestDto> getReceivedRequestsOfTheDeanOffice(Long taId);
    public List<RequestDto> getReceivedRequestsOfTheDepartment(String depName);
    List<TaDto> getAvailableTasForExam(Integer examId);

}
