package com.example.service.RequestServices;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.Role;
import com.example.entity.General.Date;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.SwapDto;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.ExamRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SwapServImpl implements SwapServ{

    private final SwapRepo swapRepo;
    private final UserRepo userRepo;
    private final ExamRepo examRepo;

    @Async("setExecutor")
    @Override
    public void createSwapRequest(SwapDto dto, Long senderId) {
        var sender = userRepo.findById(senderId)
            .orElseThrow(() -> new UserNotFoundExc(senderId));
        var receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));
        
        if (receiver.getId() == sender.getId()) {
            throw new GeneralExc("Sender and receiver cannot be the same.");
        }
        if (receiver.getRole() != Role.TA) {
            throw new GeneralExc("Receiver must be a TA member.");
        }

        // lookup your Exam however you’ve defined in ExamRepo
        var exam = examRepo.findById(dto.getExamId())
            .orElseThrow(() -> new RuntimeException("Exam not found: " + dto.getExamName()));

        Swap req = new Swap(exam);
        req.setRequestType(dto.getRequestType());
        req.setDescription(dto.getDescription());
        req.setSentTime(new Date().currenDate());
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setExam(exam);

        swapRepo.saveAndFlush(req);
    }

    @Override
    public void acceptSwapRequest(Long requestId, Long receiverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'acceptSwapRequest'");
    }

    @Override
    public void rejectSwapRequest(Long requestId, Long receiverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectSwapRequest'");
    }

    @Override
    public void cancelSwapRequest(Long requestId, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelSwapRequest'");
    }

    @Override
    public void updateSwapRequest(SwapDto dto, Long requestId, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateSwapRequest'");
    }

    @Override
    public void getSwapRequestById(Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSwapRequestById'");
    }
    
}
