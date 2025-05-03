package com.example.service.RequestServices;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Requests.LeaveDTO;



public interface LeaveServ {
    public void createLeaveRequest(LeaveDTO dto, MultipartFile file, Long senderId) throws IOException;
    public void approveLeaveRequest(Long requestId, Long approverId);
    public void rejectLeaveRequest(Long requestId, Long approverId);
    public void cancelLeaveRequest(Long requestId, Long senderId);
    public LeaveDTO getLeaveRequestById(Long requestId, Long userId);
    public List<LeaveDTO> getAllLeaveRequestsByUserId(Long userId);
    public List<LeaveDTO> getAllLeaveRequestsByApproverId(Long approverId);
    public List<LeaveDTO> getAllLeaveRequestsByStatus(String status, Long userId);
    public void updateLeaveRequest(LeaveDTO dto, Long requestId, Long userId);
    public void deleteLeaveRequest(Long requestId, Long userId);
}
