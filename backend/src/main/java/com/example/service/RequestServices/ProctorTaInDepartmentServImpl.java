package com.example.service.RequestServices;

import org.springframework.stereotype.Service;

import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.Role;
import com.example.entity.Actors.User;
import com.example.entity.Courses.Department;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.General.Faculty;
import com.example.entity.Requests.ProctorTaInDepartment;
import com.example.entity.Requests.ProctorTaInDepartmentDto;
import com.example.exception.GeneralExc;
import com.example.repo.DepartmentRepo;
import com.example.repo.ExamRepo;
import com.example.repo.FacultyRepo;
import com.example.repo.InstructorRepo;
import com.example.repo.UserRepo;
import com.example.repo.RequestRepos.ProctorTaInDepartmentRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProctorTaInDepartmentServImpl implements ProctorTaInDepartmentServ{

    private final ProctorTaInDepartmentRepo proctorTaInFacultyRepo;
    private final FacultyRepo facultyRepo;
    private final ExamRepo examRepo;
    private final InstructorRepo instrRepo;
    private final DepartmentRepo depRepo;

    @Override
    public void createProctorTaInDepartmentRequest(ProctorTaInDepartmentDto dto, Long senderId) {
        Instructor sender = instrRepo.findById(dto.getInstrId())
                .orElseThrow(() -> new GeneralExc(
                    "Sender not found: " + senderId));
        Department receiver = depRepo.findById(dto.getReceiverName())
                .orElseThrow(() -> new RuntimeException(
                    "Receiver not found: " + dto.getReceiverName()));

        Exam exam = examRepo.findByExamId(dto.getExamId())
                .orElseThrow(() -> new RuntimeException(
                    "Exam not found: " + dto.getExamName()));

        ProctorTaInDepartment req = new ProctorTaInDepartment();
        req.setRequestType(dto.getRequestType());   
        req.setDescription(dto.getDescription());   
        req.setSentTime(new Date().currenDate());     
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setExam(exam);

        proctorTaInFacultyRepo.save(req);
    }

    @Override
    public void approveProctorTaInDepartmentRequest(Long requestId, Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveProctorTaInFacultyRequest'");
    }

    @Override
    public void rejectProctorTaInDepartmentRequest(Long requestId, Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectProctorTaInFacultyRequest'");
    }

    @Override
    public void cancelProctorTaInDepartmentRequest(Long requestId, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelProctorTaInFacultyRequest'");
    }

    @Override
    public void getProctorTaInDepartmentRequestById(Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProctorTaInFacultyRequestById'");
    }

    @Override
    public void getProctorTaInDepartmentRequestBySenderId(Long senderId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProctorTaInFacultyRequestBySenderId'");
    }

    @Override
    public void updateProctorTaInDepartmentRequest(Long requestId, Long senderId, ProctorTaInDepartmentDto dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateProctorTaInFacultyRequest'");
    }
    
}
