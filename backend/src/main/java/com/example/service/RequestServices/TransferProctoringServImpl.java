package com.example.service.RequestServices;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.Role;
import com.example.entity.Actors.User;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.Requests.TransferProctoring;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.ExamRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;
import com.example.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferProctoringServImpl implements TransferProctoringServ {

    private final UserRepo userRepo;
    private final ExamRepo examRepo;
    private final TransferProctoringRepo transferRepo;

    @Async("setExecutor")
    @Override
    public void createTransferProctoringReq(TransferProctoringDto dto, Long senderId) {
        User sender = userRepo.findById(senderId)
            .orElseThrow(() -> new UserNotFoundExc(senderId));
        User receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

        if (receiver.getId() == sender.getId()) {
            throw new GeneralExc("Sender and receiver cannot be the same.");
        }
        if (receiver.getRole() != Role.TA) {
            throw new GeneralExc("Receiver must be a TA member.");
        }

        Exam exam = examRepo.findById(dto.getExamId())
                     .orElseThrow(() -> new RuntimeException("Exam not found: " + dto.getExamName()));

        TransferProctoring req = new TransferProctoring();
        req.setRequestType(dto.getRequestType());
        req.setDescription(dto.getDescription());
        req.setSentTime(new Date().currenDate());
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setExam(exam);

        transferRepo.save(req);
    }

    @Override
    public void updateTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTransferProctoringReq'");
    }

    @Override
    public void deleteTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteTransferProctoringReq'");
    }

}
