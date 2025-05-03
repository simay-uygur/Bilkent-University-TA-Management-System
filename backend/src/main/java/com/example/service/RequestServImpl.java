package com.example.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.User;
import com.example.entity.General.Date;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.RequestType;
import com.example.exception.Requests.NoSuchRequestExc;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.RequestRepo;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class RequestServImpl implements RequestServ{

    private final RequestRepo requestRepo;
    private final LeaveRepo leaveRepo;

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
    public List<Request> getProctorTaInFacultyRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.ProctorTaInFaculty);
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
    public List<Request> getProctorTaFromFacultiesRequestsOfTheUser(User u) {
        return requestRepo.findBySenderAndRequestType(u, RequestType.ProctorTaFromFaculties);
    }

    @Override
    public boolean createRequest(Request req) {
        requestRepo.save(req);
        return requestRepo.existsById(req.getRequestId().longValue());
    }

    @Override
    public Request getRequestById(Long req_id) {
        return requestRepo.findById(req_id).orElseThrow(() -> new NoSuchRequestExc(req_id));
    }

    //each day
    @Scheduled(cron = "0 0 0 * * ?")
    @Async("leaveExecutor")
    private void checkLeaveRequests() {
        List<Leave> leaves = leaveRepo.findAll();
        for (Leave leave : leaves) {
            if (leave.getDuration().getFinish().isBefore(new Date().currenDate())) {
                leave.getSender().setIsActive(true);
                leaveRepo.save(leave);
            }
        }
    }// may require notofication 
}
