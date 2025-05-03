package com.example.service.RequestServices;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Actors.User;
import com.example.entity.General.Date;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.exception.UserNotFoundExc;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveServImpl implements LeaveServ{

    private final UserRepo userRepo;
    private final LeaveRepo leaveRepo;

    @Override
    public void createLeaveRequest(LeaveDTO dto, MultipartFile file, Long senderId) throws IOException {
        User sender = userRepo.findById(senderId)
            .orElseThrow(() -> new UserNotFoundExc(senderId));
        User receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

        Leave leaveRequest = new Leave();
        Date sent_time = new Date().currenDate();
        leaveRequest.setSentTime(sent_time);
        leaveRequest.setRequestType(dto.getRequestType());
        leaveRequest.setDescription(dto.getDescription());
        leaveRequest.setDuration(dto.getDuration());
        leaveRequest.setSender(sender);
        leaveRequest.setReceiver(receiver);

        if (file != null && !file.isEmpty()) {
            leaveRequest.setAttachment(file.getBytes());
            leaveRequest.setAttachmentFilename(file.getOriginalFilename());
            leaveRequest.setAttachmentContentType(file.getContentType());
        }

        leaveRepo.save(leaveRequest);
    }

    @Override
    public void approveLeaveRequest(Long requestId, Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveLeaveRequest'");
    }

    @Override
    public void rejectLeaveRequest(Long requestId, Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectLeaveRequest'");
    }

    @Override
    public void cancelLeaveRequest(Long requestId, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelLeaveRequest'");
    }

    @Override
    public LeaveDTO getLeaveRequestById(Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLeaveRequestById'");
    }

    @Override
    public List<LeaveDTO> getAllLeaveRequestsByUserId(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllLeaveRequestsByUserId'");
    }

    @Override
    public List<LeaveDTO> getAllLeaveRequestsByApproverId(Long approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllLeaveRequestsByApproverId'");
    }

    @Override
    public List<LeaveDTO> getAllLeaveRequestsByStatus(String status, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllLeaveRequestsByStatus'");
    }

    @Override
    public void updateLeaveRequest(LeaveDTO dto, Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateLeaveRequest'");
    }

    @Override
    public void deleteLeaveRequest(Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteLeaveRequest'");
    }
    
}
