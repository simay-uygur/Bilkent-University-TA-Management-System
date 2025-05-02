package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.Actors.User;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.RequestType;
import com.example.repo.RequestRepo;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class RequestServImpl implements RequestServ{

    private final RequestRepo requestRepo;

    @Override
    public List<Request> getAllRequests() {
        return requestRepo.findAll();
    }

    @Override
    public List<Request> getRequestsOfTheUser(User u) {
        List<Request> sended = requestRepo.findByReceiver(u);
        List<Request> received = requestRepo.findBySender(u);
        received.addAll(sended);
        return received;
    }

    @Override
    public List<Request> getSwapRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.Swap);
    }

    @Override
    public List<Request> getLeaveRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.Leave);
    }

    @Override
    public List<Request> getTaInFacultyRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.TaInFaculty);
    }

    @Override
    public List<Request> getSwapEnableLeaveRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.SwapEnable);
    }

    @Override
    public List<Request> getTransferProctoringRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.TransferProctoring);
    }

    @Override
    public List<Request> getVolunteerProctoringRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.VolunteerProctoring);
    }

    @Override
    public List<Request> getTaFromOtherFacultiesRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.TaFromOtherFaculties);
    }

    @Override
    public List<Request> getProctorTaFromFacultiesRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.ProctorTaFromFaculties);
    }

    @Override
    public boolean createRequest(Request req) {
        requestRepo.save(req);
        return requestRepo.existsById(req.getRequestId().intValue());
    }

    
    
}
