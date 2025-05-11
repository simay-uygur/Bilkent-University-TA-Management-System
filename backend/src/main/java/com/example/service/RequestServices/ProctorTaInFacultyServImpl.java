package com.example.service.RequestServices;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProctorTaInFacultyServImpl implements ProctorTaInFacultyServ{
    private final ProctorTaInDepartmentRepo  depRepo;   // existing repo
    private final ProctorTaInFacultyRepo     facRepo;
    private final DepartmentRepo             departmentRepo;   // if needed

    @Transactional
    public ProctorTaInFacultyDto escalateToFaculty(Long depReqId) {

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

        facRepo.save(facReq);   // persists & assigns ID

        depRepo.save(depReq);

        return toDto(facReq);
    }

    private ProctorTaInFacultyDto toDto(ProctorTaInFaculty facReq) {
        ProctorTaInFacultyDto dto = new ProctorTaInFacultyDto();
        dto.setDepName(facReq.getSender().getName());
        dto.setExamName(facReq.getExam().getDescription());
        dto.setExamId(facReq.getExam().getExamId());
        dto.setRequiredTas(facReq.getRequiredTas());
        dto.setTasLeft(facReq.getTasLeft());
        return dto;
    }
}
