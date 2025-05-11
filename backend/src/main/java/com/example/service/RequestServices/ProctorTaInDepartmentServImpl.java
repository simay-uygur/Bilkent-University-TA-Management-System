package com.example.service.RequestServices;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.dto.TaskDto;
import com.example.entity.Actors.Instructor;
import com.example.entity.Courses.Department;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.entity.Requests.PreferTasToCourse;
import com.example.entity.Requests.PreferTasToCourseDto;
import com.example.entity.Requests.PreferTasToCourseDto.TaInfo;
import com.example.entity.Requests.ProctorTaInDepartment;
import com.example.entity.Requests.ProctorTaInDepartmentDto;
import com.example.exception.GeneralExc;
import com.example.mapper.RequestMapper;
import com.example.repo.DepartmentRepo;
import com.example.repo.ExamRepo;
import com.example.repo.FacultyRepo;
import com.example.repo.InstructorRepo;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.PreferTasToCourseRepo;
import com.example.repo.RequestRepos.ProctorTaInDepartmentRepo;
import com.example.repo.TaTaskRepo;
import com.example.service.LogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProctorTaInDepartmentServImpl implements ProctorTaInDepartmentServ{

    private final ProctorTaInDepartmentRepo proctorTaInFacultyRepo;
    private final PreferTasToCourseRepo preferTasToCourseRepo;
    private final FacultyRepo facultyRepo;
    private final ExamRepo examRepo;
    private final InstructorRepo instrRepo;
    private final DepartmentRepo depRepo;
    private final LogService log;
    private final RequestMapper mapper;
    private final TaTaskRepo taTaskRepo; 
    private final LeaveRepo leaveRepo;
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
        req.setCourseCode(dto.getCourseCode() != null ? 
                     dto.getCourseCode() : 
                     receiver.getName() + "-464"); // Default course code
    
        req.setRequiredTas(dto.getRequiredTas());
        req.setTasLeft(dto.getTasLeft());

         log.info("Creating ProctorTaInDepartment Request", 
             "Creating request with courseCode: " + req.getCourseCode() + 
             ", examId: " + dto.getExamId() + 
             ", requiredTAs: " + dto.getRequiredTas());

    proctorTaInFacultyRepo.save(req);
    }

    @Override
    public void approveProctorTaInDepartmentRequest(Long requestId, String approverId) {
        ProctorTaInDepartment req = proctorTaInFacultyRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(true);
        req.setRejected(false);
        req.setPending(false);
        log.info("Proctor TAs In Department Request Finish","Proctor TAs In Department Request with id: " +requestId+ " is finished by " + " Department with code: " + approverId);
        proctorTaInFacultyRepo.save(req);
    }

    @Override
    public void rejectProctorTaInDepartmentRequest(Long requestId, String approverId) {
        ProctorTaInDepartment req = proctorTaInFacultyRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(false);
        req.setRejected(true);
        req.setPending(false);
        log.info("Proctor TAs In Department Request Finish","Proctor TAs In Department Request with id: " +requestId+ " is finished by " + " Department with code: " + approverId);
        proctorTaInFacultyRepo.save(req);
    }

    @Override
    public void cancelProctorTaInDepartmentRequest(Long requestId, String senderId) {
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
