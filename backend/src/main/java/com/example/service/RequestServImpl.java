package com.example.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.dto.RequestDto;
import com.example.entity.Actors.TA;
import com.example.entity.Actors.User;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.RequestType;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.TransferProctoring;
import com.example.exception.Requests.NoSuchRequestExc;
import com.example.exception.UserNotFoundExc;
import com.example.mapper.RequestMapper;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.RequestRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;

import jakarta.transaction.Transactional;

import com.example.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class RequestServImpl implements RequestServ{

    private final RequestRepo requestRepo;
    private final LeaveRepo leaveRepo;
    private final UserRepo userRepo;
    private final RequestMapper reqMapper;
    private final SwapRepo swapRepo;
    private final TransferProctoringRepo transRepo;

    @Override
    public List<Request> getAllRequests() {
        return requestRepo.findAll();
    }

    @Override
    @Transactional
    public List<RequestDto> getRequestsOfTheUser(Long userId) {
        User u = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundExc(userId));
        List<RequestDto> reqs = new ArrayList<>();
        for(Request req : u.getReceivedRequests()){
            reqs.add(reqMapper.toDto(req));
        }
        return reqs;
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
    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Async("leaveExecutor")
    public void checkLeaveRequests() {
        List<Leave> leaves = leaveRepo.findAll();
        for (Leave leave : leaves) {
            if (leave.getDuration().getFinish().isBefore(new Date().currenDate())) {
                TA sender = (TA)leave.getSender();
                sender.setActive(false);
                leaveRepo.save(leave);
            }
        }
    }// may require notofication 

    @Override
    @Transactional
    @Async("setExecutor")
    public void deleteAllReceivedAndSendedSwapAndTransferRequestsBySomeTime(User u, Event duration){
    List<RequestType> want = List.of(
        RequestType.Swap,
        RequestType.TransferProctoring
    );
        /*List<Swap> recSwaps = 
        swapRepo.
        findAllByReceiverIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue
        (u.getId(), duration.getStart(), duration.getFinish(), want);
        List<Swap> senSwaps = 
        swapRepo.
        findAllBySenderIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue
        (u.getId(), duration.getStart(), duration.getFinish(), want);
        
        swapRepo.deleteAll(recSwaps);
        swapRepo.deleteAll(senSwaps);

        List<TransferProctoring> recTransfers = 
        transRepo.
        findAllByReceiverIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue
        (u.getId(), duration.getStart(), duration.getFinish(), want);
        List<TransferProctoring> senTransfers = 
        transRepo.
        findAllBySenderIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue
        (u.getId(), duration.getStart(), duration.getFinish(), want);

        transRepo.deleteAll(senTransfers);
        transRepo.deleteAll(recTransfers);*/
        swapRepo.deleteByReceiverIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
            u.getId(),
            duration.getStart(),
            duration.getFinish(),
            want
        );

        swapRepo.deleteBySenderIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
            u.getId(),
            duration.getStart(),
            duration.getFinish(),
            want
        );

        transRepo.deleteByReceiverIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
            u.getId(),
            duration.getStart(),
            duration.getFinish(),
            want
        );

        transRepo.deleteBySenderIdAndSentTimeBetweenAndRequestTypeInAndIsPendingTrue(
            u.getId(),
            duration.getStart(),
            duration.getFinish(),
            want
        );
    }
}
