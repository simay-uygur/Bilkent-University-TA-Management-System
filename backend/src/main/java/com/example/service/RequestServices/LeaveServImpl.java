package com.example.service.RequestServices;

import java.io.IOException;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.TaskDto;
import com.example.entity.Actors.Role;
import com.example.entity.Actors.TA;
import com.example.entity.Actors.User;
import com.example.entity.General.Date;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;
import com.example.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveServImpl implements LeaveServ{

    private final UserRepo userRepo;
    private final LeaveRepo leaveRepo;
    private final TARepo taRepo;
    private final TaTaskRepo taTaskRepo;

    @Async("setExecutor")
    @Override
    public void createLeaveRequest(LeaveDTO dto, MultipartFile file, Long senderId) throws IOException {
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
        if(!taRepo.existsById(userId)){
            throw new TaNotFoundExc(userId);
        }
        List<LeaveDTO> leaveDTOs = getRequestsBySenderId(userId);
        leaveDTOs.addAll(getRequestsByReceiverId(userId));
        return leaveDTOs;
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

    public List<LeaveDTO> getRequestsBySenderId(Long senderId){
        if(!taRepo.existsById(senderId)){
            throw new TaNotFoundExc(senderId);
        }
        List<Leave> leaveRequests = leaveRepo.findBySenderId(senderId).orElseThrow(() -> new GeneralExc("No leave requests found."));
        List<LeaveDTO> leaveDTOs = leaveRequests.stream()
            .map(leave -> {
                LeaveDTO dto = new LeaveDTO();
                dto.setRequestId(leave.getRequestId());
                dto.setSenderId(leave.getSender().getId());
                dto.setReceiverId(leave.getReceiver().getId());
                dto.setDescription(leave.getDescription());
                dto.setDuration(leave.getDuration());
                dto.setSentTime(leave.getSentTime());
                dto.setTasks(taTaskRepo.findTasksForTaInInterval(senderId, dto.getDuration().getStart(), dto.getDuration().getFinish()).stream()
                    .map(task -> {
                        TaskDto taskDto = new TaskDto();
                        taskDto.setDuration(task.getDuration());
                        taskDto.setType(task.getTaskType().toString());
                        return taskDto;
                    })
                    .toList());
                return dto;
            })
            .toList();
        return leaveDTOs;
    }
    
    public List<LeaveDTO> getRequestsByReceiverId(Long receiverId){
        if(!taRepo.existsById(receiverId)){
            throw new TaNotFoundExc(receiverId);
        }
        List<Leave> leaveRequests = leaveRepo.findByReceiverId(receiverId).orElseThrow(() -> new GeneralExc("No leave requests found."));
        List<LeaveDTO> leaveDTOs = leaveRequests.stream()
            .map(leave -> {
                LeaveDTO dto = new LeaveDTO();
                dto.setRequestId(leave.getRequestId());
                dto.setSenderId(leave.getSender().getId());
                dto.setReceiverId(leave.getReceiver().getId());
                dto.setDescription(leave.getDescription());
                dto.setDuration(leave.getDuration());
                dto.setSentTime(leave.getSentTime());
                dto.setTasks(taTaskRepo.findTasksForTaInInterval(leave.getSender().getId(), dto.getDuration().getStart(), dto.getDuration().getFinish()).stream()
                    .map(task -> {
                        TaskDto taskDto = new TaskDto();
                        taskDto.setDuration(task.getDuration());
                        taskDto.setType(task.getTaskType().toString());
                        return taskDto;
                    })
                    .toList());
                return dto;
            })
            .toList();
        return leaveDTOs;
    }
    
}
