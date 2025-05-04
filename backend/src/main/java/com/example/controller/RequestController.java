package com.example.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;            
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Actors.Instructor;
import com.example.entity.Requests.LeaveDTO;
import com.example.entity.Requests.ProctorTaFromFacultiesDto;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.RequestDto;
import com.example.entity.Requests.SwapDto;
import com.example.entity.Requests.SwapEnableDto;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.entity.Requests.WorkLoadDto;
import com.example.exception.UserNotFoundExc;
import com.example.mapper.RequestMapper;
import com.example.repo.InstructorRepo;
import com.example.repo.UserRepo;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.ProctorTaFromFacultiesRepo;
import com.example.repo.RequestRepos.ProctorTaInFacultyRepo;
import com.example.repo.RequestRepos.RequestRepo;
import com.example.repo.RequestRepos.SwapEnableRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;
import com.example.service.RequestServices.LeaveServ;
import com.example.service.RequestServices.ProctorTaFromFacultiesServ;
import com.example.service.RequestServices.ProctorTaInFacultyServ;
import com.example.service.RequestServices.SwapEnableServ;
import com.example.service.RequestServices.SwapServ;
import com.example.service.RequestServices.TransferProctoringServ;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestController {

    private final SwapServ swapServ;
    private final SwapRepo swapRepo;
    private final SwapEnableServ swapEnableServ;
    private final SwapEnableRepo swapEnableRepo;
    private final LeaveServ leaveServ;
    private final LeaveRepo leaveRepo;
    private final ProctorTaFromFacultiesServ proctorTaFromFacultiesServ;
    private final ProctorTaFromFacultiesRepo fromFacRepo;
    private final ProctorTaInFacultyServ proctorTaInFacultyServ;
    private final ProctorTaInFacultyRepo inFacRepo;
    private final TransferProctoringServ transferProctoringServ;
    private final TransferProctoringRepo transferRepo;

    private final InstructorRepo insRepo;
    private final RequestMapper requestMapper;
    
    @PostMapping(
        path = "/ta/{taId}/request/leave",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE   // ← **must** declare this
    )
    public ResponseEntity<Void> sendLeaveRequest(
        @PathVariable Long taId,
        @RequestPart("data") LeaveDTO dto,   
        @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        leaveServ.createLeaveRequest(dto, file, taId);
        boolean exists = leaveRepo.existsBySenderIdAndReceiverId(taId, dto.getReceiverId());
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/ta/{taId}/request/leave/all")
    public ResponseEntity<List<LeaveDTO>> getLeaveRequests(@PathVariable Long taId) {
        return new ResponseEntity<>(leaveServ.getAllLeaveRequestsByUserId(taId), HttpStatus.OK);
    }

    @GetMapping("/instructor/{insId}/request/received/all")
    public ResponseEntity<List<RequestDto>> getAllForReceiver(
            @PathVariable Long insId
    ) {
        Instructor instructor = insRepo.findById(insId)
                .orElseThrow(() -> new UserNotFoundExc(insId));

        // fetch polymorphic Requests
        List<Request> entities = instructor.getReceived_requests();

        // map to DTOs
        List<RequestDto> dtos = entities.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/swap")
    public ResponseEntity<?> sendSwap(
        @PathVariable Long taId,
        @RequestBody SwapDto dto
    ) {
        swapServ.createSwapRequest(dto, taId);
        boolean exists = swapRepo.existsBySenderIdAndReceiverId(taId, dto.getReceiverId());
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/swap-enable")
    public ResponseEntity<Void> sendSwapEnable(
            @PathVariable Long taId,
            @RequestBody SwapEnableDto dto
    ) {
        swapEnableServ.createSwapEnableReq(dto, taId);
        boolean exists = swapEnableRepo.existsBySenderIdAndReceiverId(taId, dto.getReceiverId());
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/transfer-proctoring")
    public ResponseEntity<Void> sendTransferProctoring(
            @PathVariable Long taId,
            @RequestBody TransferProctoringDto dto
    ) {
        transferProctoringServ.createTransferProctoringReq(dto, taId);
        boolean exists = transferRepo.existsBySenderIdAndReceiverId(taId, dto.getReceiverId());
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/proctor-from-faculties")
    public ResponseEntity<Void> sendProctorTaFromFaculties(
            @PathVariable Long taId,
            @RequestBody ProctorTaFromFacultiesDto dto
    ) {
        proctorTaFromFacultiesServ.createProctorTaFromFacultiesRequest(dto, taId);
        boolean exists = fromFacRepo.existsByExamIdAndSenderId(taId, dto.getExamid());
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/proctor-in-faculty")
    public ResponseEntity<Void> sendProctorTaInFaculty(
            @PathVariable Long taId,
            @RequestBody ProctorTaInFacultyDto dto
    ) {
        proctorTaInFacultyServ.createProctorTaInFacultyRequest(dto, taId);
        boolean exists = inFacRepo.existsBySenderIdAndReceiverId(taId, dto.getReceiverId());
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/ta/{taId}/request/workload")
    public ResponseEntity<Void> sendWorkloadRequest(
        @PathVariable Long taId,
        @RequestBody WorkLoadDto dto
    ) {

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