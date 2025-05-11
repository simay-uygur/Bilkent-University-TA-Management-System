package com.example.service;

import java.util.*;
import java.util.stream.Collectors;

import com.example.dto.TaDto;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Semester;
import com.example.mapper.TaMapper;
import com.example.repo.*;
import com.example.util.TaAvailabilityChecker;
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
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.PreferTasToCourseRepo;
import com.example.repo.RequestRepos.ProctorTaFromFacultiesRepo;
import com.example.repo.RequestRepos.ProctorTaFromOtherFacultyRepo;
import com.example.repo.RequestRepos.ProctorTaInDepartmentRepo;
import com.example.repo.RequestRepos.RequestRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;
import com.example.repo.RequestRepos.WorkLoadRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
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
    private final TaMapper taMapper;
    private final CourseOfferingRepo offeringRepo;
    private final TaAvailabilityChecker availabilityChecker;
    private final LogService log;

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
        List<Integer> overlapping = examRepo.findOverlappingExamIds(
        duration.getStart(), duration.getFinish());
        // 2) delete swaps
        int recSwaps = swapRepo.deleteAllSwapsForTaAndExamIds(u.getId(), overlapping);

        // 3) delete transfers
        int trans = transRepo.deleteAllSwapsForTaAndExamIds(u.getId(), overlapping);

        log.info
        ("Deletion", "Due to assigning to the ta with id: " + 
        u.getId() + " new task with duration: " + duration + 
        " total number of deleted swap requests and transfer requests that collide with the duration is: swaps:" + recSwaps + ", transfers:" + trans);
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

    // for transferring or swapping
    @Override
    public List<TaDto> getAvailableTasForExam(Integer examId) {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + examId));

        // 1) grab the Semester & Department of this exam
        CourseOffering myOff = exam.getCourseOffering();
        Semester semester     = myOff.getSemester();
        Department dept       = myOff.getCourse().getDepartment();

        // 2) fetch all offerings in that dept & term
        List<CourseOffering> allOfferings
                = offeringRepo.findByCourse_Department_NameAndSemester_YearAndSemester_Term(dept.getName(), semester.getYear(), semester.getTerm());

        // 3) collect all assigned TAs, de-duplicate
        Set<TA> candidates = allOfferings.stream()
                .flatMap(off -> off.getAssignedTas().stream())
                .collect(Collectors.toSet());

        // 4) filter out anyone with a duty or lesson conflict
        return candidates.stream()
                .filter(ta -> availabilityChecker.isAvailable(ta, exam.getDuration()))
                .map(taMapper::toDto)
                .toList();
    }



}
