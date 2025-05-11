package com.example.service.RequestServices;

import java.util.ArrayList;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.DeanOffice;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromFacultiesDto;
import com.example.entity.Requests.ProctorTaFromOtherFaculty;
import com.example.entity.Requests.ProctorTaFromOtherFacultyDto;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.DeanOfficeRepo;
import com.example.repo.ExamRepo;
import com.example.repo.FacultyRepo;
import com.example.repo.RequestRepos.ProctorTaFromFacultiesRepo;
import com.example.repo.RequestRepos.ProctorTaFromOtherFacultyRepo;
import com.example.repo.UserRepo;
import com.example.service.LogService;
import com.example.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProctorTaFromOtherFacultiesServImpl implements ProctorTaFromFacultiesServ {

    private final UserRepo userRepo;
    private final ProctorTaFromFacultiesRepo fromFacRepo;
    private final ProctorTaFromOtherFacultyRepo inFacRepo;
    private final FacultyRepo facultyRepo;
    private final ExamRepo examRepo;
    private final DeanOfficeRepo deanRepo;
    private final LogService log;
    private final NotificationService notServ;
    @Async("setExecutor")
    @Override
    public void createProctorTaFromFacultiesRequest(ProctorTaFromFacultiesDto dto, Long senderId) {
        // 1. lookup sender & receiver
        DeanOffice sender = deanRepo.findById(senderId)
                .orElseThrow(() -> new UserNotFoundExc(senderId));

        // 2. create & save parent
        ProctorTaFromFaculties parent = new ProctorTaFromFaculties();
        parent.setRequestType(dto.getRequestType());
        parent.setDescription(dto.getDescription());
        parent.setSentTime(new Date().currenDate());
        parent.setSender(sender);
        parent.setRequiredTas(dto.getRequiredTas());
        parent.setTasLeft(parent.getRequiredTas());
        parent.setCourseCode(dto.getCourseCode());
        Exam exam = examRepo.findByExamId(dto.getExamId())
                    .orElseThrow(() -> new GeneralExc(
                        "Exam not found: " + dto.getExamName()));
        parent.setExam(exam);
        parent.setProctorTaFromOtherFacs(new ArrayList<>()); 
        // 3. for each child DTO: create & save a ProctorTaInFaculty
        for (ProctorTaFromOtherFacultyDto childDto : dto.getProctorTaInFacultyDtos()) {
            DeanOffice faculty = deanRepo.findById(childDto.getReceiverId())
                    .orElseThrow(() -> new GeneralExc(
                        "Faculty not found: " + childDto.getReceiverName()));

            

            ProctorTaFromOtherFaculty child = new ProctorTaFromOtherFaculty();
            child.setRequestType(dto.getRequestType());    // share the same type
            child.setDescription(dto.getDescription());    // or child‑specific?
            child.setSentTime(parent.getSentTime());       // link by timestamp
            child.setSender(sender);
            child.setReceiver(faculty);
            child.setExam(exam);
            child.setProctorTaFromFaculties(parent);
            parent.getProctorTaFromOtherFacs().add(child);
        }
        log.info("Prefer Tas from other Faculties Request creation","Dean Office member with id: " + senderId + " is sent to other Faculties");
        fromFacRepo.save(parent); // save parent again to update the relationship
        notServ.notifyCreation(parent);
    }

    @Override
    public void approveProctorTaFromFacultiesRequest(Long requestId, Long approverId) {
        ProctorTaFromFaculties req = fromFacRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(true);
        req.setRejected(false);
        req.setPending(false);
        log.info("Proctor TAs from other Faculties Request Finish","Proctor TAs from other Faculties Request with id: " +requestId+ " is finished by " + " Dean Office member with id: " + approverId);
        fromFacRepo.save(req);
        notServ.notifyApproval(req);
    }

    @Override
    public void rejectProctorTaFromFacultiesRequest(Long requestId, Long approverId) {
        ProctorTaFromFaculties req = fromFacRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(false);
        req.setRejected(true);
        req.setPending(false);
        log.info("Proctor TAs from other Faculties Request Finish","Proctor TAs from other Faculties Request with id: " +requestId+ " is finished by " + " Dean Office member with id: " + approverId);
        fromFacRepo.save(req);
        notServ.notifyRejection(req);
    }

    @Override
    public void cancelProctorTaFromFacultiesRequest(Long requestId, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelProctorTaFromFacultiesRequest'");
    }

    @Override
    public ProctorTaFromFaculties getProctorTaFromFacultiesRequestById(Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProctorTaFromFacultiesRequestById'");
    }

    @Override
    public void deleteProctorTaFromFacultiesRequest(Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteProctorTaFromFacultiesRequest'");
    }

    @Override
    public void updateProctorTaFromFacultiesRequest(ProctorTaFromFacultiesDto dto, Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateProctorTaFromFacultiesRequest'");
    }
    
    
}
