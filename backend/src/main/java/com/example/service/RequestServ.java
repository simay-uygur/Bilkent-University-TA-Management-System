package com.example.service;

import java.util.List;

import com.example.dto.RequestDto;
import com.example.dto.TaDto;
import com.example.entity.Actors.User;
import com.example.entity.General.Event;
import com.example.entity.Requests.LeaveDTO;
import com.example.entity.Requests.PreferTasToCourseDto;
import com.example.entity.Requests.ProctorTaFromOtherFacultyDto;
import com.example.entity.Requests.ProctorTaInDepartmentDto;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.SwapDto;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.entity.Requests.WorkLoadDto;

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

    public List<ProctorTaInDepartmentDto> getReceivedProctorInDepRequestsOfTheDep(String depName);
    public List<PreferTasToCourseDto> getReceivedPreferTasToCourseRequestsOfTheDep(String depName);
    public List<LeaveDTO> getReceivedLeaveRequestsOfTheDep(String depName);

    public List<ProctorTaInFacultyDto> getReceivedProctoTaInFacultyOfTheDean(Long id);
    public List<ProctorTaFromOtherFacultyDto> getReceivedProctorTaFromOtherFacOfTheDean(Long id);

    public List<WorkLoadDto> getReceivedWorkLoadRequestsOfTheInstr(Long id);

    public List<TransferProctoringDto> getReceivedProctoringRequestsOfTheTa(Long id);
    public List<SwapDto> getReceivedSwapProctoringRequestsOfTheTa(Long id);
}
