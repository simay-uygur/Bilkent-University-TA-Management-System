package com.example.service.RequestServices;

import java.io.IOException;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.TaskDto;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Department;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.exception.GeneralExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.repo.DepartmentRepo;
import com.example.repo.ExamRepo;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;
import com.example.service.LogService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveServImpl implements LeaveServ{

    private final LeaveRepo leaveRepo;
    private final TARepo taRepo;
    private final TaTaskRepo taTaskRepo;
    private final DepartmentRepo depRepo;
    private final ExamRepo examRepo;
    private final LogService log;
    @Async("setExecutor")
    @Override
    public void createLeaveRequest(LeaveDTO dto, MultipartFile file, Long senderId) throws IOException {
        TA sender = taRepo.findById(senderId).
        orElseThrow(() -> new TaNotFoundExc(dto.getSenderId()));
        Department department = depRepo.findById(dto.getDepName()).
        orElseThrow(() -> new GeneralExc("Deparment with name " + dto.getDepName() + " not found"));

        Leave leaveRequest = new Leave();
        Date sent_time = new Date().currenDate();
        leaveRequest.setCourseCode(dto.getCourseCode());
        leaveRequest.setSentTime(sent_time);
        leaveRequest.setRequestType(dto.getRequestType());
        leaveRequest.setDescription(dto.getDescription());
        leaveRequest.setDuration(dto.getDuration());
        leaveRequest.setSender(sender);
        leaveRequest.setReceiver(department);

        if (file != null && !file.isEmpty()) {
            leaveRequest.setAttachment(file.getBytes());
            leaveRequest.setAttachmentFilename(file.getOriginalFilename());
            leaveRequest.setAttachmentContentType(file.getContentType());
        }
        log.info("Leave Request Creation","Leave request is created by TA with id: " + senderId + " and sent to " +dto.getDepName()+ " Department");
        leaveRepo.save(leaveRequest);
    }

    @Override
    @Transactional
    public boolean approveLeaveRequest(Long requestId, Long approverId) {
        Leave req = leaveRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        TA sender = req.getSender();
        taTaskRepo.deleteTaTasksForTaInInterval(sender.getId(), req.getDuration().getStart(), req.getDuration().getFinish());
        sender.getExams().clear();
        List<Exam> exams = examRepo.findAllByTaId(sender.getId());
        for (Exam exam : exams) {
            exam.getAssignedTas().remove(sender);
            examRepo.save(exam);
        }
        sender.setActive(false);
        req.setRejected(false);
        req.setApproved(true);
        req.setPending(false);
        log.info("Leave Request Approval","Leave request with id: " +requestId+ " is approved by " +req.getReceiver().getName()+ " Department");
        taRepo.saveAndFlush(sender);
        leaveRepo.save(req);
        return true;
    }

    @Override
    public void rejectLeaveRequest(Long requestId, Long approverId) {
        Leave req = leaveRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such leave request."));
        req.setApproved(false);
        req.setRejected(true);
        req.setPending(false);
        log.info("Leave Request Rejection","Leave request with id: " +requestId+ " is rejected by " +req.getReceiver().getName()+ " Department");
        leaveRepo.save(req);
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
        leaveDTOs.addAll(getAllLeaveRequestsByUserId(userId));
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

    @Override
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
                dto.setDepName(leave.getReceiver().getName());
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

    @Override
    public List<LeaveDTO> getRequestsByReceiverName(String receiverId){
        if(!depRepo.existsById(receiverId)){
            throw new GeneralExc("Department with name " + receiverId + " not found!");
        }
        List<Leave> leaveRequests = leaveRepo.findByReceiverName(receiverId).orElseThrow(() -> new GeneralExc("No leave requests found."));
        List<LeaveDTO> leaveDTOs = leaveRequests.stream()
            .map(leave -> {
                LeaveDTO dto = new LeaveDTO();
                dto.setRequestId(leave.getRequestId());
                dto.setSenderId(leave.getSender().getId());
                dto.setDepName(leave.getReceiver().getName());
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
