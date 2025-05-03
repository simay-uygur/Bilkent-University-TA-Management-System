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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Actors.User;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.General.Faculty;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromFacultiesDto;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.SwapDto;
import com.example.entity.Requests.SwapEnable;
import com.example.entity.Requests.SwapEnableDto;
import com.example.entity.Requests.TransferProctoring;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.exception.UserNotFoundExc;
import com.example.repo.ExamRepo;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.ProctorTaFromFacultiesRepo;
import com.example.repo.RequestRepos.ProctorTaInFacultyRepo;
import com.example.repo.RequestRepos.SwapEnableRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;
import com.example.repo.UserRepo;
import com.example.service.RequestServ;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestController {
    
    private final RequestServ reqServ;
    private final UserRepo   userRepo;
    private final ExamRepo   examRepo; // Assuming you have an ExamRepo for exam lookups
    private final SwapRepo   swapRepo; // Assuming you have a SwapRepo for swap requests
    private final SwapEnableRepo swapEnableRepo; // Assuming you have a SwapEnableRepo for swap enable requests
    private final LeaveRepo leaveRepo; // Assuming you have a LeaveRepo for leave requests
    private final TransferProctoringRepo transferRepo; // Assuming you have a TransferProctoringRepo for transfer requests
    private final ProctorTaFromFacultiesRepo fromFacRepo; // Assuming you have a ProctorTaFromFacultiesRepo for proctor requests
    private final ProctorTaInFacultyRepo inFacRepo; // Assuming you have a ProctorTaInFacultyRepo for proctor requests

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
            dto.setSenderId(leave.getSender().getId());
            dto.setReceiverName(leave.getReceiver().getName() + " " + leave.getReceiver().getSurname());
            dto.setSenderName(leave.getSender().getName() + " " + leave.getSender().getSurname());
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

    @PostMapping("/swap")
    public ResponseEntity<SwapDto> sendSwap(
        @PathVariable Long taId,
        @RequestBody SwapDto dto
    ) {
        var sender = userRepo.findById(taId)
            .orElseThrow(() -> new UserNotFoundExc(taId));
        var receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

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

        swapRepo.save(req);

        // map back to DTO
        dto.setRequestId(req.getRequestId());
        dto.setSenderName(sender.getName() + " " + sender.getSurname());
        dto.setSentTime(req.getSentTime());
        // duration isn’t persisted – leave it untouched or handle as you prefer

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(dto);
    }

    @PostMapping("/swap-enable")
    public ResponseEntity<SwapEnableDto> sendSwapEnable(
            @PathVariable Long taId,
            @RequestBody SwapEnableDto dto
    ) {
        User sender = userRepo.findById(taId)
            .orElseThrow(() -> new UserNotFoundExc(taId));
        User receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

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

        dto.setRequestId(req.getRequestId());
        dto.setSenderName(
            sender.getName() + " " + sender.getSurname()
        );
        dto.setSentTime(req.getSentTime());

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/transfer-proctoring")
    public ResponseEntity<TransferProctoringDto> sendTransferProctoring(
            @PathVariable Long taId,
            @RequestBody TransferProctoringDto dto
    ) {
        User sender = userRepo.findById(taId)
            .orElseThrow(() -> new UserNotFoundExc(taId));
        User receiver = userRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

            Exam exam = examRepo.findById(dto.getExamId())
            .orElseThrow(() -> new RuntimeException("Exam not found: " + dto.getExamName()));

        TransferProctoring req = new TransferProctoring();
        req.setRequestType(dto.getRequestType());
        req.setDescription(dto.getDescription());
        req.setSentTime(new Date().currenDate());
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setExam(exam);
        req.setRequiredTas(dto.getRequiredTas());

        transferRepo.save(req);

        dto.setRequestId(req.getRequestId());
        dto.setSenderName(
            sender.getName() + " " + sender.getSurname()
        );
        dto.setSentTime(req.getSentTime());

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

     @PostMapping("/proctor-from-faculties")
    public ResponseEntity<ProctorTaFromFacultiesDto> sendProctorTaFromFaculties(
            @PathVariable Long taId,
            @RequestBody ProctorTaFromFacultiesDto dto
    ) {
        // 1. lookup sender & receiver
        User sender = userRepo.findById(taId)
                .orElseThrow(() -> new UserNotFoundExc(taId));
        User receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

        // 2. create & save parent
        ProctorTaFromFaculties parent = new ProctorTaFromFaculties();
        parent.setRequestType(dto.getRequestType());
        parent.setDescription(dto.getDescription());
        parent.setSentTime(new Date().currentDate());
        parent.setSender(sender);
        parent.setReceiver(receiver);
        fromFacRepo.save(parent);

        // 3. for each child DTO: create & save a ProctorTaInFaculty
        for (ProctorTaInFacultyDto childDto : dto.getProctorTaInFacultyDtos()) {
            Faculty faculty = facultyRepo.findByName(childDto.getFacultyName())
                    .orElseThrow(() -> new RuntimeException(
                        "Faculty not found: " + childDto.getFacultyName()));

            Exam exam = examRepo.findByExamName(childDto.getExamName())
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
            inFacRepo.save(child);
        }

        // 4. echo back the parent DTO with generated ID & timestamp
        dto.setRequestId(parent.getRequestId());
        dto.setSentTime(parent.getSentTime());
        dto.setSenderName(sender.getName() + " " + sender.getSurname());
        dto.setReceiverName(receiver.getName() + " " + receiver.getSurname());

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
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