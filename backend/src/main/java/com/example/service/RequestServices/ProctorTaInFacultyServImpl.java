package com.example.service.RequestServices;

import com.example.entity.General.Date;
import com.example.entity.Requests.RequestType;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.DeanOffice;
import com.example.entity.Courses.Department;
import com.example.entity.Requests.ProctorTaInDepartment;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.exception.GeneralExc;
import com.example.repo.DepartmentRepo;
import com.example.repo.RequestRepos.ProctorTaInDepartmentRepo;
import com.example.repo.RequestRepos.ProctorTaInFacultyRepo;
import com.example.service.LogService;
import com.example.service.NotificationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProctorTaInFacultyServImpl implements ProctorTaInFacultyServ{
    private final ProctorTaInDepartmentRepo  depRepo;   // existing repo
    private final ProctorTaInFacultyRepo     facRepo;
    private final DepartmentRepo             departmentRepo;   // if needed
    private final LogService log;
    private final NotificationService notServ;
    @Transactional
    @Override
    public boolean escalateToFaculty(Long depReqId) {

        // 1) fetch the department-level request
        ProctorTaInDepartment depReq = depRepo.findById(depReqId)
            .orElseThrow(() -> new GeneralExc("No such department request"));

        // 2) nothing to do if already satisfied
        if (depReq.getTasLeft() <= 0) {
            throw new GeneralExc("Request already fulfilled – nothing to escalate");
        }

        Department senderDep = depReq.getReceiver();                 // department is now the sender
        DeanOffice deanOffice = senderDep.getFaculty().getDeanOffice();
        if (deanOffice == null) {
            throw new GeneralExc("Faculty does not have an assigned DeanOffice");
        }

        ProctorTaInFaculty facReq = new ProctorTaInFaculty();
        facReq.setSender(senderDep);
        facReq.setReceiver(deanOffice);
        facReq.setExam(depReq.getExam());
        facReq.setRequiredTas(depReq.getRequiredTas());
        facReq.setTasLeft(depReq.getTasLeft());
        facReq.setCourseCode(depReq.getCourseCode());
        facReq.setExam( depReq.getExam() );
        facReq.setDescription( depReq.getDescription());

        facReq.setRequestType(RequestType.ProctorTaInFaculty);   // pick the enum you use
        //facReq.setDescription(depReq.getDescription());             // keep the old text
        facReq.setCourseCode(depReq.getCourseCode());               // e.g.  "CS-464"
        facReq.setInstrId(depReq.getSender().getId());

        // after you create facReq:
        facReq.setInstrId(depReq.getSender().getId()); // for now it is null

        LocalDateTime now = LocalDateTime.now();
        Date time = new Date();
        time.setYear(now.getYear());
        time.setMonth(now.getMonthValue());
        time.setDay(now.getDayOfMonth());
        time.setHour(now.getHour());
        time.setMinute(now.getMinute());
        facReq.setSentTime(time);
        //facReq.setApproved(false);

        //toDto(facReq)

        facRepo.save(facReq);   // persists & assigns ID
        depRepo.delete(depReq); // delete from the department
        notServ.notifyCreation(facReq);
        log.info("Proctor Ta in Department Request with id: "+depReqId+" is Transferred to the Faculty", "");
        return true;
    }
    private ProctorTaInFacultyDto toDto(ProctorTaInFaculty r) {

        ProctorTaInFacultyDto dto = new ProctorTaInFacultyDto();


        dto.setRequestId   (r.getRequestId());
        dto.setRequestType (r.getRequestType());
        dto.setDescription (r.getDescription());
        dto.setSenderName  (r.getSender().getName());                 // department name
        dto.setReceiverName(r.getReceiver().getName());           // dean-office user’s name, adapt if needed
        dto.setSentTime    (r.getSentTime());
        dto.setCourseCode  (r.getCourseCode());
        dto.setPending     (r.isPending());
        dto.setApproved    (r.isApproved());
        dto.setRejected    (r.isRejected());


        dto.setDepName     (r.getSender().getName());
        dto.setFacultyName (r.getSender().getFaculty().getCode());
        dto.setDean_id     (r.getReceiver().getId());
        dto.setInstrId     (r.getInstrId());
        dto.setExamName    (r.getExam().getDescription());
        dto.setExamId      (r.getExam().getExamId());
        dto.setRequiredTas (r.getRequiredTas());
        dto.setTasLeft     (r.getTasLeft());

        return dto;
    }
//
//    private ProctorTaInFacultyDto toDto(ProctorTaInFaculty facReq) {
//        ProctorTaInFacultyDto dto = new ProctorTaInFacultyDto();
//        dto.setDepName(facReq.getSender().getName());
//        dto.setExamName(facReq.getExam().getDescription());
//        dto.setExamId(facReq.getExam().getExamId());
//        dto.setRequiredTas(facReq.getRequiredTas());
//        dto.setTasLeft(facReq.getTasLeft());
//        return dto;
//    }

    @Override
    public boolean approve(Long reqId, Long approverId) {
        ProctorTaInFaculty req = facRepo.findById(reqId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(true);
        req.setRejected(false);
        req.setPending(false);
        log.info("Proctor TAs In Faculty Request Finish","Proctor TAs in Faculty Request with id: " +reqId+ " is finished by " + " DeanOffice member with id: " + approverId);
        facRepo.save(req);
        notServ.notifyApproval(req);
        return true;
    }

    @Override
    public boolean reject(Long reqId, Long rejecterId) {
        ProctorTaInFaculty req = facRepo.findById(reqId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(false);
        req.setRejected(true);
        req.setPending(false);
        log.info("Proctor TAs In Faculty Request Finish","Proctor TAs in Faculty Request with id: " +reqId+ " is finished by " + " DeanOffice member with id: " + rejecterId);
        facRepo.save(req);
        notServ.notifyRejection(req);
        return true;
    }
}
