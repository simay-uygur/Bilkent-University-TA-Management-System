package com.example.service.RequestServices;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.Role;
import com.example.entity.Actors.User;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.Requests.SwapEnable;
import com.example.entity.Requests.SwapEnableDto;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.ExamRepo;
import com.example.repo.RequestRepos.SwapEnableRepo;
import com.example.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SwapEnableServImpl implements SwapEnableServ{

    private final UserRepo userRepo;
    private final ExamRepo examRepo;
    private final SwapEnableRepo swapEnableRepo;

    @Async("setExecutor")
    @Override
    public void createSwapEnableReq(SwapEnableDto dto, Long senderId) {
        User sender = userRepo.findById(senderId)
            .orElseThrow(() -> new UserNotFoundExc(senderId));
        User receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

        if (receiver.getId() == sender.getId()) {
            throw new GeneralExc("Sender and receiver cannot be the same.");
        }
        if (receiver.getRole() != Role.DEPARTMENT_STAFF) {
            throw new GeneralExc("Receiver must be a staff member.");
        }

        Exam exam = examRepo.findById(dto.getExamId())
            .orElseThrow(() -> new RuntimeException("Exam not found: " + dto.getExamName()));

        SwapEnable req = new SwapEnable(exam);
        req.setRequestType(dto.getRequestType());
        req.setDescription(dto.getDescription());
        req.setSentTime(new Date().currenDate());
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setExam(exam);

        swapEnableRepo.save(req);
    }

    @Override
    public void updateSwapEnableReq(SwapEnableDto swapEnableDto, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateSwapEnableReq'");
    }

    @Override
    public void deleteSwapEnableReq(SwapEnableDto swapEnableDto, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteSwapEnableReq'");
    }
    
}
