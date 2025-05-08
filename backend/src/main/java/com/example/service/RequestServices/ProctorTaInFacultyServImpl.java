package com.example.service.RequestServices;

import org.springframework.stereotype.Service;

import com.example.entity.Actors.Role;
import com.example.entity.Actors.User;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.General.Faculty;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.exception.GeneralExc;
import com.example.repo.ExamRepo;
import com.example.repo.FacultyRepo;
import com.example.repo.UserRepo;
import com.example.repo.RequestRepos.ProctorTaInFacultyRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProctorTaInFacultyServImpl implements ProctorTaInFacultyServ{

    private final ProctorTaInFacultyRepo proctorTaInFacultyRepo;
    private final FacultyRepo facultyRepo;
    private final ExamRepo examRepo;
    private final UserRepo userRepo;

    @Override
    public void createProctorTaInFacultyRequest(ProctorTaInFacultyDto dto, Long senderId) {
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException(
                    "Sender not found: " + senderId));
        User receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new RuntimeException(
                    "Receiver not found: " + dto.getReceiverId()));
        if (receiver.getId() == sender.getId()) {
            throw new GeneralExc("Sender and receiver cannot be the same.");
        }
        if (receiver.getRole() != Role.DEANS_OFFICE) {
            throw new GeneralExc("Receiver must be a faculty member.");
        }
        Faculty faculty = facultyRepo.findByCode(dto.getFacultyName())
                    .orElseThrow(() -> new RuntimeException(
                        "Faculty not found: " + dto.getFacultyName()));

        Exam exam = examRepo.findByExamId(dto.getExamId())
                .orElseThrow(() -> new RuntimeException(
                    "Exam not found: " + dto.getExamName()));

        ProctorTaInFaculty req = new ProctorTaInFaculty();
        req.setRequestType(dto.getRequestType());   
        req.setDescription(dto.getDescription());   
        req.setSentTime(new Date().currenDate());     
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setFaculty(faculty);
        req.setExam(exam);

        proctorTaInFacultyRepo.save(req);
    }

    @Override
    public void approveProctorTaInFacultyRequest(Long requestId, Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveProctorTaInFacultyRequest'");
    }

    @Override
    public void rejectProctorTaInFacultyRequest(Long requestId, Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectProctorTaInFacultyRequest'");
    }

    @Override
    public void cancelProctorTaInFacultyRequest(Long requestId, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelProctorTaInFacultyRequest'");
    }

    @Override
    public void getProctorTaInFacultyRequestById(Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProctorTaInFacultyRequestById'");
    }

    @Override
    public void getProctorTaInFacultyRequestBySenderId(Long senderId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProctorTaInFacultyRequestBySenderId'");
    }

    @Override
    public void updateProctorTaInFacultyRequest(Long requestId, Long senderId, ProctorTaInFacultyDto dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateProctorTaInFacultyRequest'");
    }
    
}
