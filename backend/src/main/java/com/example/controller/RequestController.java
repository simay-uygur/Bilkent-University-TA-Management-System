package com.example.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;            
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.RequestDto;
import com.example.dto.SwapOptionDto;
import com.example.dto.TAAssignmentRequest;
import com.example.entity.Actors.Instructor;
import com.example.entity.Requests.LeaveDTO;
import com.example.entity.Requests.PreferTasToCourseDto;
import com.example.entity.Requests.ProctorTaFromFacultiesDto;
import com.example.entity.Requests.ProctorTaFromOtherFacultyDto;
import com.example.entity.Requests.ProctorTaInDepartmentDto;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.SwapDto;
import com.example.entity.Requests.TransferCandidateDto;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.entity.Requests.WorkLoad;
import com.example.entity.Requests.WorkLoadDto;
import com.example.exception.UserNotFoundExc;
import com.example.mapper.RequestMapper;
import com.example.mapper.Requests.PreferTasToCourseMapper;
import com.example.repo.InstructorRepo;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.ProctorTaFromFacultiesRepo;
import com.example.repo.RequestRepos.ProctorTaInDepartmentRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;
import com.example.repo.RequestRepos.WorkLoadRepo;
import com.example.service.RequestServ;
import com.example.service.RequestServices.LeaveServ;
import com.example.service.RequestServices.PreferTasToCourseServ;
import com.example.service.RequestServices.ProctorTaFromFacultiesServ;
import com.example.service.RequestServices.ProctorTaInDepartmentServ;
import com.example.service.RequestServices.SwapServ;
import com.example.service.RequestServices.TransferProctoringServ;
import com.example.service.RequestServices.WorkLoadServ;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestController {

    private final SwapServ swapServ;
    private final SwapRepo swapRepo;
    private final LeaveServ leaveServ;
    private final LeaveRepo leaveRepo;
    private final ProctorTaFromFacultiesServ proctorTaFromFacultiesServ;
    private final ProctorTaFromFacultiesRepo fromFacRepo;
    private final ProctorTaInDepartmentRepo inDepRepo;
    private final ProctorTaInDepartmentServ proctorTaInDepartmentServ;
    private final TransferProctoringServ transferProctoringServ;
    private final TransferProctoringRepo transferRepo;
    private final WorkLoadServ workLoadServ;
    private final WorkLoadRepo workLoadRepo;
    private final PreferTasToCourseMapper preferTasToCourseMapper;
    private final PreferTasToCourseServ preferTasToCourseServ;

    private final RequestServ requestService;

    private final InstructorRepo insRepo;
    private final RequestMapper requestMapper;

    private final PreferTasToCourseServ prefService;

    // Get all requests sent to a department
    @GetMapping("/department/{depName}/preferTas")
    public ResponseEntity<List<PreferTasToCourseDto>> getByDepartment(
            @PathVariable String depName) {
        List<PreferTasToCourseDto> requests =
            prefService.getRequestsOfTheDeparment(depName);
        return ResponseEntity.ok(requests);
    }

    // Get all requests created by an instructor
    @GetMapping("/instructor/{instrId}/preferTas")
    public ResponseEntity<List<PreferTasToCourseDto>> getByInstructor(
            @PathVariable Long instrId) {
              List<PreferTasToCourseDto> requests =
            prefService.getRequestsOfTheInstructor(instrId);
        return ResponseEntity.ok(requests);
    }

    // Create a new preference request
    @PostMapping(
        path = "/instructor/{instrId}/section/{sectionCode}/preferTas"
    )
    public ResponseEntity<Void> createReferTasRequest(
            @RequestBody TAAssignmentRequest dto,
            @PathVariable Long instrId,
            @PathVariable String sectionCode) {
        prefService.createRequest(dto.getPreferredTas(), dto.getNonPreferredTas(), dto.getTaNeeded(), instrId, sectionCode);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    /*public ResponseEntity<Boolean> createRequest(
      @PathVariable Long instrId, 
      @PathVariable String sectionCode, 
      @RequestBody PreferTasToCourseDto dto){
        prefService.createRequest(dto.getPreferredTas(), dto.getNonPreferredTas(), dto.getTaNeeded(), instrId, sectionCode);
    }*/
    // Get all requests sent to a department
    @GetMapping("/request/{reqId}")
    public ResponseEntity<RequestDto> getRequestById(
            @PathVariable Long reqId) {
        Request request = requestService.getRequestById(reqId);
        return ResponseEntity.ok(requestMapper.toDto(request));
    }
    @GetMapping("/request/preferTas/{reqId}")
    public ResponseEntity<PreferTasToCourseDto> getPreferTasRequestById(
            @PathVariable Long reqId) {
        return ResponseEntity.ok((prefService.getRequestById(reqId)));
    }
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    
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
        boolean exists = leaveRepo.existsBySenderIdAndReceiverNameAndIsRejected(taId, dto.getDepName(), false);
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/instructor/{instrId}/section/{sectionCode}/leave/{id}/approve")
    public ResponseEntity<Boolean> approveLeaveRequest(@PathVariable Long instrId, @PathVariable Long id) {
        return new ResponseEntity<>(leaveServ.approveLeaveRequest(id, instrId),HttpStatus.I_AM_A_TEAPOT);
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
        List<WorkLoad> entities = instructor.getReceivedWorkloadRequests();
        // map to DTOs
        List<RequestDto> dtos = entities.stream().map(requestMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/ta/{taId}/swap")
    public CompletableFuture<ResponseEntity<Boolean>> sendSwap(
        @PathVariable Long taId,
        @RequestBody SwapDto dto
    ) {
        return swapServ.createSwapRequest(dto, taId).thenApply(success -> {
          if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
          } else {
            return ResponseEntity
                  .status(HttpStatus.BAD_REQUEST)
                  .body(false);
          }
      });
    }

    @PutMapping("ta/{taId}/swap/{swapId}/approve")
    public ResponseEntity<Boolean> approveSwapRequest(@PathVariable Long swapId, @PathVariable Long taId) {
      return new ResponseEntity<>(swapServ.acceptSwapRequest(swapId, taId),HttpStatus.OK);
    }

    @PutMapping("ta/{approverId}/departmentproctor/{reqId}/approve")
    public void finishApproveTAInDepRequest(@PathVariable Long reqId,@PathVariable String approverId) {
      proctorTaInDepartmentServ.approveProctorTaInDepartmentRequest(reqId, approverId);
    }
    @PutMapping("ta/{approverId}/departmentproctor/{reqId}/reject")
    public void finishRejectTAInDepRequest(@PathVariable Long reqId,@PathVariable String approverId) {
      proctorTaInDepartmentServ.rejectProctorTaInDepartmentRequest(reqId, approverId);
    }
     @PutMapping("ta/prefertas/{reqId}/approve")
    public void finishApproveCourseTaRequest(@PathVariable Long reqId) {
      preferTasToCourseServ.approve(reqId);
    }

    @PostMapping("/transfer-proctoring")
    public ResponseEntity<Void> sendTransferProctoring(
            @PathVariable Long taId,
            @RequestBody TransferProctoringDto dto
    ) {
        transferProctoringServ.createTransferProctoringReq(dto, taId);
        boolean exists = transferRepo.existsBySenderIdAndReceiverIdAndExamExamIdAndIsRejected(taId, dto.getReceiverId(), dto.getExamId(), false);
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/proctor-from-faculties") //Deans to Deans
    public ResponseEntity<Void> sendProctorTaFromFaculties(
            @PathVariable Long taId,
            @RequestBody ProctorTaFromFacultiesDto dto
    ) {
        proctorTaFromFacultiesServ.createProctorTaFromFacultiesRequest(dto, taId);
        boolean exists = fromFacRepo.existsBySenderIdAndExamExamIdAndIsRejected(taId, dto.getExamId(), false);
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/proctor-in-faculty-department/{instrId}") // Ins to Department
    public ResponseEntity<Void> sendProctorTaInDepartment(
            @PathVariable Long instrId,
            @RequestBody ProctorTaInDepartmentDto dto
    ) {
        proctorTaInDepartmentServ.createProctorTaInDepartmentRequest(dto, instrId);
        boolean exists = inDepRepo
        .existsBySender_IdAndReceiver_NameAndExam_ExamIdAndIsRejected(
            instrId,                     // the pathVariable
            dto.getReceiverName(),       // your DTO’s getter
            dto.getExamId(),
            false
          );

        return new ResponseEntity<>(
            exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/ta/{taId}/request/workload")
    public ResponseEntity<Void> sendWorkloadRequest(
        @PathVariable Long taId,
        @RequestBody WorkLoadDto dto
    ) {
        workLoadServ.createWorkLoad(dto, taId);
        boolean exists = workLoadRepo.existsBySenderIdAndTaskTaskIdAndIsRejected(taId, dto.getTaskId(), false);
        return new ResponseEntity<>(exists ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/instructor/{instrId}/requests/workload/{requestId}/approve")
    public ResponseEntity<Boolean> approveWorkloadRequest(@PathVariable Long instrId, @RequestBody Long requestId) {
        return new ResponseEntity<>(workLoadServ.approveRequest(instrId, requestId),HttpStatus.ACCEPTED);
    }

    @PutMapping("/instructor/{instrId}/requests/workload/{requestId}/reject")
    public ResponseEntity<Boolean> rejectWorkloadRequest(@PathVariable Long instrId, @RequestBody Long requestId) {
        return new ResponseEntity<>(workLoadServ.rejectRequest(instrId, requestId),HttpStatus.ACCEPTED);
    }
    
    @GetMapping("/instr/{instrId}/receivedAll")
    public ResponseEntity<List<RequestDto>> getAllReceivedRequestsOfTheInstructor(@PathVariable Long instrId){
      return new ResponseEntity<>(requestService.getReceivedRequestsOfTheInstructor(instrId), HttpStatus.OK);
    }

    @GetMapping("/ta/{taId}/receivedAll")
    public ResponseEntity<List<RequestDto>> getAllReceivedRequestsOfTheTa(
            @PathVariable Long taId) {
        return ResponseEntity.ok(
          requestService.getReceivedRequestsOfTheTa(taId)
        );
    }

    // ——— Dean’s Office ———
    @GetMapping("/deanoffice/{deanOfficeId}/receivedAll")
    public ResponseEntity<List<RequestDto>> getAllReceivedRequestsOfTheDeanOffice(
            @PathVariable Long deanOfficeId) {
        return ResponseEntity.ok(
           requestService.getReceivedRequestsOfTheDeanOffice(deanOfficeId)
        );
    }

    // ——— Department ———
    @GetMapping("/department/{deptName}/receivedAll")
    public ResponseEntity<List<RequestDto>> getAllReceivedRequestsOfTheDepartment(
            @PathVariable String deptName) {
        return ResponseEntity.ok(
          requestService.getReceivedRequestsOfTheDepartment(deptName)
        );
    }


    @GetMapping("/{user_id}/receivedReqs")
    public ResponseEntity<List<RequestDto>> getReceivedRequests(@PathVariable Long user_id) {
      return new ResponseEntity<>(requestService.getReceivedRequestsOfTheUser(user_id), HttpStatus.ACCEPTED);
    }

    @GetMapping("/ta/{taId}/exam/{examId}/swap/getAvailableTas")
    public CompletableFuture<ResponseEntity<List<SwapOptionDto>>> getAvailableTasForSwapping(@PathVariable Long taId, @PathVariable int examId) {
        return swapServ.findSwapCandidates(taId, examId).thenApply(tasList -> {
            if (tasList != null) {
              return ResponseEntity.ok(tasList);
            } else {
                return ResponseEntity.notFound().build();
            }
        });
    }

    @GetMapping("/ta/{taId}/exam/{examId}/transfer/getAvailableTas")
    public CompletableFuture<ResponseEntity<List<TransferCandidateDto>>> getAvailableTasForTransferring(@PathVariable Long taId, @PathVariable int examId) {
      return transferProctoringServ.findTransferCandidates(taId, examId).thenApply(tasList -> {
          if (tasList != null) {
            return ResponseEntity.ok(tasList);
          } else {
              return ResponseEntity.notFound().build();
          }
      });
    }

    // 1) DeanOffice: Proctor‐in‐Faculty
    @GetMapping("/deanOffice/{deanId}/proctor-in-faculty")
    public List<ProctorTaInFacultyDto> getReceivedProctorTaInFaculty(
            @PathVariable("deanId") Long deanId) {
        return requestService.getReceivedProctoTaInFacultyOfTheDean(deanId);
    }

    // 2) DeanOffice: Proctor‐from‐Other‐Faculty
    @GetMapping("/deanOffice/{deanId}/proctor-from-other-faculty")
    public List<ProctorTaFromOtherFacultyDto> getReceivedProctorTaFromOtherFaculty(
            @PathVariable("deanId") Long deanId) {
        return requestService.getReceivedProctorTaFromOtherFacOfTheDean(deanId);
    }

    // 3) Instructor: Workload requests
    @GetMapping("/instructors/{instrId}/workload-requests")
    public List<WorkLoadDto> getReceivedWorkloadRequestsOfTheInstructor(
            @PathVariable("instrId") Long instructorId) {
        return requestService.getReceivedWorkLoadRequestsOfTheInstr(instructorId);
    }

    // 4) TA: Transfer‐proctoring requests
    @GetMapping("/ta/{taId}/transfer-proctoring-requests")
    public List<TransferProctoringDto> getReceivedTransferProctoringOfTheTa(
            @PathVariable("taId") Long taId) {
        return requestService.getReceivedProctoringRequestsOfTheTa(taId);
    }

    // 5) TA: Swap‐proctoring requests
    @GetMapping("/ta/{taId}/swap-proctoring-requests")
    public List<SwapDto> getReceivedSwapProctoringOfTheTa(
            @PathVariable("taId") Long taId) {
        return requestService.getReceivedSwapProctoringRequestsOfTheTa(taId);
    }

    // 6) Department: Proctor‐in‐Department requests
    @GetMapping("/departments/{depName}/proctor-in-department-requests")
    public List<ProctorTaInDepartmentDto> getReceivedProctorInDeptRequests(
            @PathVariable("depName") String depName) {
        return requestService.getReceivedProctorInDepRequestsOfTheDep(depName);
    }

    // 7) Department: Prefer‐TAs‐to‐Course requests
    @GetMapping("/departments/{depName}/prefer-tas-to-course-requests")
    public List<PreferTasToCourseDto> getReceivedPreferTasToCourseRequests(
            @PathVariable("depName") String depName) {
        return requestService.getReceivedPreferTasToCourseRequestsOfTheDep(depName);
    }

    // 8) Department: Leave requests
    @GetMapping("/departments/{depName}/leave-requests")
    public List<LeaveDTO> getReceivedLeaveRequestsOfTheDep(
            @PathVariable("depName") String depName) {
        return requestService.getReceivedLeaveRequestsOfTheDep(depName);
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

/*Swap*/
/*SwapEnable*/
/*{
  "requestType": "SwapEnable",
  "description": "Enable swap for my exam assignment",
  "receiverId": 20310,
  "examId": 550,
  "examName": "CS315 Final",
  "duration": {
    "start": {
      "day": 15,
      "month": 5,
      "year": 2025,
      "hour": 14,
      "minute": 0
    },
    "finish": {
      "day": 15,
      "month": 5,
      "year": 2025,
      "hour": 16,
      "minute": 0
    }
  }
}*/
/*TransferProctoring*/
/*{
  "requestType": "TransferProctoring",
  "description": "Please transfer my proctoring duty to another session",
  "receiverId": 20320,
  "examId": 560,
  "examName": "CS319 Midterm",
  "duration": {
    "start": {
      "day": 20,
      "month": 5,
      "year": 2025,
      "hour": 10,
      "minute": 0
    },
    "finish": {
      "day": 20,
      "month": 5,
      "year": 2025,
      "hour": 12,
      "minute": 0
    }
  }
}
*/
/*ProctorTaFromFaculties*/
/*{
  "requestType": "ProctorTaFromFaculties",
  "description": "Requesting proctoring from other faculties",
  "receiverId": 20330,
  "examid": 570,
  "examName": "CS401 Project Presentation",
  "proctorTaInFacultyDtos": [
    {
      "facultyName": "Engineering Faculty",
      "examId": 570,
      "examName": "CS401 Project Presentation",
      "requiredTas": 2
    },
    {
      "facultyName": "Science Faculty",
      "examId": 570,
      "examName": "CS401 Project Presentation",
      "requiredTas": 1
    }
  ]
}
*/
/*ProctorTaInFaculty*/
/*{
  "requestType": "ProctorTaInFaculty",
  "description": "I can proctor for another faculty’s exam",
  "senderId": 30020,
  "receiverId": 20330,
  "examId": 580,
  "examName": "CS320 Lab Exam",
  "facultyName": "Science Faculty",
  "requiredTas": 1
}
*/
/*WorkLoad*/
/*
{
  "requestType": "WorkLoad",
  "description": "Requesting workload adjustment",
  "receiverId": 20340,
  "taskId": 900,
  "taskType": "LAB_SUPERVISION",
  "workload": 3,
  "duration": {
    "start": {
      "day":  5,
      "month":  6,
      "year": 2025,
      "hour": 8,
      "minute": 30
    },
    "finish": {
      "day": 5,
      "month": 6,
      "year": 2025,
      "hour": 12,
      "minute": 0
    }
  }
}
*/