package com.example.service.RequestServices;

import org.springframework.stereotype.Service;

import com.example.entity.Actors.User;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.General.Faculty;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromFacultiesDto;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.exception.UserNotFoundExc;
import com.example.repo.ExamRepo;
import com.example.repo.FacultyRepo;
import com.example.repo.UserRepo;
import com.example.repo.RequestRepos.ProctorTaFromFacultiesRepo;
import com.example.repo.RequestRepos.ProctorTaInFacultyRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProctorTaFromOtherFacultiesServImpl implements ProctorTaFromFacultiesServ {

    private final UserRepo userRepo;
    private final ProctorTaFromFacultiesRepo fromFacRepo;
    private final ProctorTaInFacultyRepo inFacRepo;
    private final FacultyRepo facultyRepo;
    private final ExamRepo examRepo;

    @Override
    public void createProctorTaFromFacultiesRequest(ProctorTaFromFacultiesDto dto, Long senderId) {
        // 1. lookup sender & receiver
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new UserNotFoundExc(senderId));
        User receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

        // 2. create & save parent
        ProctorTaFromFaculties parent = new ProctorTaFromFaculties();
        parent.setRequestType(dto.getRequestType());
        parent.setDescription(dto.getDescription());
        parent.setSentTime(new Date().currenDate());
        parent.setSender(sender);
        parent.setReceiver(receiver);
        fromFacRepo.save(parent);

        // 3. for each child DTO: create & save a ProctorTaInFaculty
        for (ProctorTaInFacultyDto childDto : dto.getProctorTaInFacultyDtos()) {
            Faculty faculty = facultyRepo.findByCode(childDto.getFacultyName())
                    .orElseThrow(() -> new RuntimeException(
                        "Faculty not found: " + childDto.getFacultyName()));

            Exam exam = examRepo.findByExamId(childDto.getExamId())
                    .orElseThrow(() -> new RuntimeException(
                        "Exam not found: " + childDto.getExamName()));

            ProctorTaInFaculty child = new ProctorTaInFaculty();
            child.setRequestType(dto.getRequestType());    // share the same type
            child.setDescription(dto.getDescription());    // or child‑specific?
            child.setSentTime(parent.getSentTime());       // link by timestamp
            child.setSender(sender);
            child.setReceiver(receiver);
            child.setFaculty(faculty);
            child.setExam(exam);
            parent.getProctorTaInFaculties().add(inFacRepo.save(child));
        }

        fromFacRepo.save(parent); // save parent again to update the relationship
    }

    @Override
    public void approveProctorTaFromFacultiesRequest(Long requestId, Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveProctorTaFromFacultiesRequest'");
    }

    @Override
    public void rejectProctorTaFromFacultiesRequest(Long requestId, Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectProctorTaFromFacultiesRequest'");
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
