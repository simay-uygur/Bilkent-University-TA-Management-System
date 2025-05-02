package com.example.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;               // ← correct import
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Actors.User;
import com.example.entity.General.Date;
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
        @RequestPart("data") LeaveDTO dto,   
        @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        User sender = userRepo.findById(taId)
            .orElseThrow(() -> new UserNotFoundExc(taId));
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

        boolean created = reqServ.createRequest(leaveRequest);
        return ResponseEntity.status(created ? 201 : 400).body(created);
    }

    // src/controller/RequestController.java
    @GetMapping("/ta/{taId}/request/leave")
    public ResponseEntity<List<LeaveDTO>> getLeaveRequests(
            @PathVariable Long taId) {

        // 1) load user, then leave entities
        User ta = userRepo.findById(taId)
            .orElseThrow(() -> new UserNotFoundExc(taId));

        List<Leave> leaves = reqServ.getLeaveRequestsOfTheUser(ta)
                                    .stream()
                                    .map(r -> (Leave) r)
                                    .collect(Collectors.toList());

        // 2) map to DTO including attachment
        List<LeaveDTO> dtos = leaves.stream().map(leave -> {
            LeaveDTO dto = new LeaveDTO();
            dto.setRequestId(leave.getRequestId());
            dto.setRequestType(leave.getRequestType());
            dto.setDescription(leave.getDescription());
            dto.setReceiverId(leave.getReceiver().getId());
            // map your start/finish fields...
            dto.setDuration(leave.getDuration());
            dto.setSentTime(leave.getSentTime());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    // do not run, run only in local database
    @GetMapping("/request/leave/{requestId}/attachment")
    @Transactional(readOnly = true)   
    public ResponseEntity<byte[]> downloadLeaveAttachment(
        @PathVariable Long requestId) {
        // 1) Load your Leave entity from the service/repo
        Leave leave = (Leave) reqServ.getRequestById(requestId);
        byte[] data = leave.getAttachment();
        if (data == null || data.length == 0) {
            return ResponseEntity.noContent().build();
        }
        // 2) Set proper headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
            MediaType.parseMediaType(leave.getAttachmentContentType()));
        headers.setContentDisposition(
            ContentDisposition.attachment()
            .filename(leave.getAttachmentFilename())
            .build()
        );

    // 3) Return the raw bytes
    return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
/*
Leave Request
{"requestType":"Leave",
  "description":"Medical leave — PNG attached",
"receiverId":20300,
"duration": {
    "start": {
      "day": 28,
      "month": 3,
      "year": 2025,
      "hour": 1,
      "minute": 0
    },
    "finish": {
      "day": 28,
      "month": 3,
      "year": 2025,
      "hour": 1,
      "minute": 5
    }
  }
} */