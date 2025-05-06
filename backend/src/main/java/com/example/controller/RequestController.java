package com.example.controller;

import java.io.IOException;

import org.springframework.http.MediaType;               // ← correct import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Actors.User;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.exception.UserNotFoundExc;
import com.example.repo.UserRepo;
import com.example.service.RequestServ;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestController {
    
    private final RequestServ reqServ;
    private final UserRepo   userRepo;

    @PostMapping(
        path = "/ta/{taId}/request/leave",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE   // ← **must** declare this
    )
    public ResponseEntity<Boolean> sendLeaveRequest(
        @PathVariable Long taId,
        @RequestPart("data") LeaveDTO dto,               // ← name matches form‐data key “data”
        @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        User sender = userRepo.findById(taId)
            .orElseThrow(() -> new UserNotFoundExc(taId));
        User receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

        Leave leaveRequest = new Leave();
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

        boolean created = reqServ.createRequest(leaveRequest);
        return ResponseEntity.status(created ? 201 : 400).body(created);
    }
}
