package com.example.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.dto.RequestDto;
import com.example.entity.Actors.DeanOffice;
import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Actors.User;
import com.example.entity.Courses.Department;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.ProctorTaFromOtherFaculty;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.RequestType;
import com.example.exception.Requests.NoSuchRequestExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.mapper.RequestMapper;
import com.example.repo.DeanOfficeRepo;
import com.example.repo.DepartmentRepo;
import com.example.repo.ExamRepo;
import com.example.repo.InstructorRepo;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.PreferTasToCourseRepo;
import com.example.repo.RequestRepos.ProctorTaFromFacultiesRepo;
import com.example.repo.RequestRepos.ProctorTaFromOtherFacultyRepo;
import com.example.repo.RequestRepos.ProctorTaInDepartmentRepo;
import com.example.repo.RequestRepos.RequestRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;
import com.example.repo.RequestRepos.WorkLoadRepo;
import com.example.repo.TARepo;
import com.example.repo.UserRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j
@RequiredArgsConstructor
public class RequestServImpl implements RequestServ{

    private final DeanOfficeRepo deanOfficeRepo;

    private final RequestRepo requestRepo;
    private final UserRepo userRepo;
    private final InstructorRepo instructorRepo;
    private final RequestMapper reqMapper;

    private final LeaveRepo leaveRepo;
    private final SwapRepo swapRepo;
    private final TransferProctoringRepo transRepo;
    private final ProctorTaFromFacultiesRepo prTaFrFacsRepo;
    private final ProctorTaFromOtherFacultyRepo prTaFrOtherFacRepo;
    private final ProctorTaInDepartmentRepo prTaInDepRepo;
    private final WorkLoadRepo workloadRepo;
    private final PreferTasToCourseRepo prefTasToCourseRepo;
    
    private final TARepo taRepo;
    private final RequestMapper mapper;
    private final ExamRepo examRepo;
    private final DepartmentRepo deptRepo;

    @Override
    public List<Request> getAllRequests() {
        return requestRepo.findAll();
    }

    @Override
    @Transactional
    public List<RequestDto> getReceivedRequestsOfTheUser(Long userId) {
        TA ta = taRepo.findById(userId).orElseThrow(() -> new TaNotFoundExc(userId));
        
        List<RequestDto> all = new ArrayList<>();

        // swaps
        all.addAll(
        ta.getReceivedSwapRequests()
                .stream()
                .map(mapper::toDto)
                .toList()
        );

        all.addAll(
        ta.getReceivedTransferRequests()
            .stream()
            .map(mapper::toDto)
            .toList()
        );

        // transfer‐proctoring
        /*all.addAll(
        ta.getSendedSwapRequests()
                    .stream()
                    .map(mapper::toDto)
                    .toList()
        );*/

        // sort by sentTime (oldest first)
        all.sort(Comparator.comparing(RequestDto::getSentTime));
        return all;
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
                TA sender = leave.getSender();
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

        List<Integer> overlapping = examRepo.findOverlappingExamIds(
        duration.getStart(), duration.getFinish());
        log.info("overlapping exams = {}", overlapping);
        // 2) delete swaps
        int recSwaps = swapRepo.deleteAllSwapsForTaAndExamIds(u.getId(), overlapping);

        // 3) delete transfers
        int trans = transRepo.deleteAllSwapsForTaAndExamIds(u.getId(), overlapping);

        log.info("cleanup: recSwaps={}, sentSwaps={}, recTrans={}, sentTrans={}",
            recSwaps, trans);
  }

    @Transactional
    @Override
    public List<RequestDto> getReceivedRequestsOfTheTa(Long taId) {
        TA ta = taRepo.findById(taId)
                      .orElseThrow(() -> new NoSuchElementException("TA not found: " + taId));

        List<RequestDto> dtos = new ArrayList<>();

        // 1) Swap requests received by this TA
        swapRepo
          .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(taId)
          .forEach(r -> dtos.add(mapper.toDto(r)));

        // 2) Transfer Proctoring requests received by this TA
        transRepo
          .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(taId)
          .forEach(r -> dtos.add(mapper.toDto(r)));

        return dtos;
    }

    @Transactional
    @Override
    public List<RequestDto> getReceivedRequestsOfTheInstructor(Long instructorId) {
        Instructor instr = instructorRepo.findById(instructorId)
                 .orElseThrow(() -> new NoSuchElementException(
                     "Instructor not found: " + instructorId
                 ));

        List<RequestDto> dtos = new ArrayList<>();

        // 2) WorkLoad requests
        workloadRepo
          .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(instructorId)
          .forEach(req -> dtos.add(mapper.toDto(req)));

        return dtos;

    }

    @Transactional
    @Override
    public List<RequestDto> getReceivedRequestsOfTheDeanOffice(Long deanOfficeId) {
        DeanOffice dean = deanOfficeRepo.findById(deanOfficeId)
            .orElseThrow(() -> new NoSuchElementException(
                "DeanOffice not found: " + deanOfficeId
            ));

        // 2) fetch all pending ProctorTaFromOtherFaculty for that receiver
        List<ProctorTaFromOtherFaculty> reqs =
            prTaFrOtherFacRepo.findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(
                deanOfficeId
            );

        // 3) map to DTOs
        return reqs.stream()
                   .map(mapper::toDto)
                   .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<RequestDto> getReceivedRequestsOfTheDepartment(String deptName) {
        Department dep = deptRepo.findById(deptName)
                .orElseThrow(() -> new NoSuchElementException(
                    "Department not found: " + deptName
                ));

        List<RequestDto> dtos = new ArrayList<>();

        // 2) pending Proctor‐in‐Dept requests
        prTaInDepRepo
          .findByReceiver_NameAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(deptName)
          .forEach(r -> dtos.add(mapper.toDto(r)));

        // 3) pending Prefer‐TA‐to‐Course requests
        prefTasToCourseRepo
          .findByReceiver_NameAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(deptName)
          .forEach(r -> dtos.add(mapper.toDto(r)));

        return dtos;
    }

}
