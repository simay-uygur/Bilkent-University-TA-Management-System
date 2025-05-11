package com.example.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.dto.RequestDto;
import com.example.dto.TaDto;
import com.example.dto.TaskDto;
import com.example.entity.Actors.DeanOffice;
import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Actors.User;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Department;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.General.Semester;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.entity.Requests.PreferTasToCourse;
import com.example.entity.Requests.PreferTasToCourseDto;
import com.example.entity.Requests.PreferTasToCourseDto.TaInfo;
import com.example.entity.Requests.ProctorTaFromOtherFaculty;
import com.example.entity.Requests.ProctorTaFromOtherFacultyDto;
import com.example.entity.Requests.ProctorTaInDepartment;
import com.example.entity.Requests.ProctorTaInDepartmentDto;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.RequestType;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.SwapDto;
import com.example.entity.Requests.TransferProctoring;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.entity.Requests.WorkLoad;
import com.example.entity.Requests.WorkLoadDto;
import com.example.exception.Requests.NoSuchRequestExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.mapper.RequestMapper;
import com.example.mapper.TaMapper;
import com.example.repo.CourseOfferingRepo;
import com.example.repo.DeanOfficeRepo;
import com.example.repo.DepartmentRepo;
import com.example.repo.ExamRepo;
import com.example.repo.InstructorRepo;
import com.example.repo.RequestRepos.LeaveRepo;
import com.example.repo.RequestRepos.PreferTasToCourseRepo;
import com.example.repo.RequestRepos.ProctorTaFromFacultiesRepo;
import com.example.repo.RequestRepos.ProctorTaFromOtherFacultyRepo;
import com.example.repo.RequestRepos.ProctorTaInDepartmentRepo;
import com.example.repo.RequestRepos.ProctorTaInFacultyRepo;
import com.example.repo.RequestRepos.RequestRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;
import com.example.repo.RequestRepos.WorkLoadRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;
import com.example.repo.UserRepo;
import com.example.util.TaAvailabilityChecker;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class RequestServImpl implements RequestServ{

    private final DeanOfficeRepo deanOfficeRepo;
    private final RequestRepo requestRepo;
    private final UserRepo userRepo;
    private final InstructorRepo instructorRepo;
    private final RequestMapper reqMapper;
    private final LeaveRepo leaveRepo;
    private final SwapRepo swapRepo;
    private final TransferProctoringRepo transRepo;
    private final ProctorTaFromFacultiesRepo prTaFrFacsRepo;
    private final ProctorTaFromOtherFacultyRepo prTaFrOtherFacRepo;
    private final ProctorTaInDepartmentRepo prTaInDepRepo;
    private final WorkLoadRepo workloadRepo;
    private final PreferTasToCourseRepo prefTasToCourseRepo;
    private final TARepo taRepo;
    private final RequestMapper mapper;
    private final ExamRepo examRepo;
    private final DepartmentRepo deptRepo;
    private final TaMapper taMapper;
    private final CourseOfferingRepo offeringRepo;
    private final TaAvailabilityChecker availabilityChecker;
    private final LogService log;
    private final ProctorTaInDepartmentRepo proctorTaInDepRepo;
    private final TaTaskRepo taTaskRepo;
    private final ProctorTaInFacultyRepo proctorTaInFacultyRepo;
    @Override
    public List<Request> getAllRequests() {
        return requestRepo.findAll();
    }

    @Override
    @Transactional
    public List<RequestDto> getReceivedRequestsOfTheUser(Long userId) {
        TA ta = taRepo.findById(userId).orElseThrow(() -> new TaNotFoundExc(userId));
        
        List<RequestDto> all = new ArrayList<>();

        // swaps
        all.addAll(
        ta.getReceivedSwapRequests()
                .stream()
                .map(mapper::toDto)
                .toList()
        );

        all.addAll(
        ta.getReceivedTransferRequests()
            .stream()
            .map(mapper::toDto)
            .toList()
        );

        // transfer‐proctoring
        /*all.addAll(
        ta.getSendedSwapRequests()
                    .stream()
                    .map(mapper::toDto)
                    .toList()
        );*/

        // sort by sentTime (oldest first)
        all.sort(Comparator.comparing(RequestDto::getSentTime));
        return all;
    }
    
    @Override
    public boolean createRequest(Request req) {
        requestRepo.save(req);
        return requestRepo.existsById(req.getRequestId().longValue());
    }

    @Override
    public Request getRequestById(Long req_id) {
        return requestRepo.findById(req_id).orElseThrow(() -> new NoSuchRequestExc(req_id));
    }

    //each day
    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Async("leaveExecutor")
    public void checkLeaveRequests() {
        List<Leave> leaves = leaveRepo.findAll();
        for (Leave leave : leaves) {
            if (leave.getDuration().getFinish().isBefore(new Date().currenDate())) {
                TA sender = leave.getSender();
                sender.setActive(false);
                leaveRepo.save(leave);
            }
        }
    }// may require notofication 

    @Override
    @Transactional
    @Async("setExecutor")
    public void deleteAllReceivedAndSendedSwapAndTransferRequestsBySomeTime(User u, Event duration){
    List<RequestType> want = List.of(
        RequestType.Swap,
        RequestType.TransferProctoring
    );
        List<Integer> overlapping = examRepo.findOverlappingExamIds(
        duration.getStart(), duration.getFinish());
        // 2) delete swaps
        int recSwaps = swapRepo.deleteAllSwapsForTaAndExamIds(u.getId(), overlapping);

        // 3) delete transfers
        int trans = transRepo.deleteAllSwapsForTaAndExamIds(u.getId(), overlapping);

        log.info
        ("Deletion", "Due to assigning to the ta with id: " + 
        u.getId() + " new task with duration: " + duration + 
        " total number of deleted swap requests and transfer requests that collide with the duration is: swaps:" + recSwaps + ", transfers:" + trans);
  }

    @Transactional
    @Override
    public List<RequestDto> getReceivedRequestsOfTheTa(Long taId) {
        TA ta = taRepo.findById(taId)
                      .orElseThrow(() -> new NoSuchElementException("TA not found: " + taId));

        List<RequestDto> dtos = new ArrayList<>();

        // 1) Swap requests received by this TA
        swapRepo
          .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(taId)
          .forEach(r -> dtos.add(mapper.toDto(r)));

        // 2) Transfer Proctoring requests received by this TA
        transRepo
          .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(taId)
          .forEach(r -> dtos.add(mapper.toDto(r)));

        return dtos;
    }

    @Transactional
    @Override
    public List<RequestDto> getReceivedRequestsOfTheInstructor(Long instructorId) {
        Instructor instr = instructorRepo.findById(instructorId)
                 .orElseThrow(() -> new NoSuchElementException(
                     "Instructor not found: " + instructorId
                 ));

        List<RequestDto> dtos = new ArrayList<>();

        // 2) WorkLoad requests
        workloadRepo
          .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(instructorId)
          .forEach(req -> dtos.add(mapper.toDto(req)));

        return dtos;

    }

    @Transactional
    @Override
    public List<RequestDto> getReceivedRequestsOfTheDeanOffice(Long deanOfficeId) {
        DeanOffice dean = deanOfficeRepo.findById(deanOfficeId)
            .orElseThrow(() -> new NoSuchElementException(
                "DeanOffice not found: " + deanOfficeId
            ));

        // 2) fetch all pending ProctorTaFromOtherFaculty for that receiver
        List<ProctorTaFromOtherFaculty> reqs =
            prTaFrOtherFacRepo.findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(
                deanOfficeId
            );

        // 3) map to DTOs
        return reqs.stream()
                   .map(mapper::toDto)
                   .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<RequestDto> getReceivedRequestsOfTheDepartment(String deptName) {
        Department dep = deptRepo.findById(deptName)
                .orElseThrow(() -> new NoSuchElementException(
                    "Department not found: " + deptName
                ));

        List<RequestDto> dtos = new ArrayList<>();

        // 2) pending Proctor‐in‐Dept requests
        prTaInDepRepo
          .findByReceiver_NameAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(deptName)
          .forEach(r -> dtos.add(mapper.toDto(r)));

        // 3) pending Prefer‐TA‐to‐Course requests
        prefTasToCourseRepo
          .findByReceiver_NameAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(deptName)
          .forEach(r -> dtos.add(mapper.toDto(r)));

        return dtos;
    }

    // for transferring or swapping
    @Override
    public List<TaDto> getAvailableTasForExam(Integer examId) {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + examId));

        // 1) grab the Semester & Department of this exam
        CourseOffering myOff = exam.getCourseOffering();
        Semester semester     = myOff.getSemester();
        Department dept       = myOff.getCourse().getDepartment();

        // 2) fetch all offerings in that dept & term
        List<CourseOffering> allOfferings
                = offeringRepo.findByCourse_Department_NameAndSemester_YearAndSemester_Term(dept.getName(), semester.getYear(), semester.getTerm());

        // 3) collect all assigned TAs, de-duplicate
        Set<TA> candidates = allOfferings.stream()
                .flatMap(off -> off.getAssignedTas().stream())
                .collect(Collectors.toSet());

        // 4) filter out anyone with a duty or lesson conflict
        return candidates.stream()
                .filter(ta -> availabilityChecker.isAvailable(ta, exam.getDuration()))
                .map(taMapper::toDto)
                .toList();
    }
    /////////////
    @Override
    public List<ProctorTaInDepartmentDto> getReceivedProctorInDepRequestsOfTheDep(String depName){
        Department dep = deptRepo.findById(depName)
                .orElseThrow(() -> new NoSuchElementException(
                    "Department not found: " + depName
                ));

        // 2) pending Proctor‐in‐Dept requests
        return proctorTaInDepRepo
          .findByReceiver_NameAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(depName)
          .stream()
          .map(this::toDto)
          .toList();
    }

    @Override
    public List<PreferTasToCourseDto> getReceivedPreferTasToCourseRequestsOfTheDep(String depName){
        Department dep = deptRepo.findById(depName)
                .orElseThrow(() -> new NoSuchElementException(
                    "Department not found: " + depName
                ));
        return prefTasToCourseRepo
                .findByReceiver_NameAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(depName)
                .stream().map(this::toDto).toList();
    }
    @Override
    public List<LeaveDTO> getReceivedLeaveRequestsOfTheDep(String depName)
    {
        Department dep = deptRepo.findById(depName)
                .orElseThrow(() -> new NoSuchElementException(
                    "Department not found: " + depName
        ));

        return leaveRepo.
        findByReceiver_NameAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(depName)
        .stream()
        .map(this::toDto)
        .toList();
    }

    private ProctorTaInDepartmentDto toDto(ProctorTaInDepartment e) {
        ProctorTaInDepartmentDto dto = new ProctorTaInDepartmentDto();
        dto.setCourseCode(e.getCourseCode());
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setRequestType(e.getRequestType());
        dto.setPending(e.isPending());
        dto.setDescription(e.getExam().getDescription());
        dto.setReceiverName(e.getReceiver().getName());
        dto.setInstrId(e.getSender().getId());
        dto.setExamId(e.getExam().getExamId());
        dto.setExamName(e.getExam().getDescription());
        dto.setRequiredTas(e.getRequiredTas());
        dto.setTasLeft(e.getTasLeft());
        return dto;
    }

    private PreferTasToCourseDto toDto(PreferTasToCourse e){
        PreferTasToCourseDto dto = new PreferTasToCourseDto();
        dto.setCourseCode(e.getCourseCode());
        dto.setDescription(e.getDescription());
        dto.setRequestType(e.getRequestType());
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setSenderName(e.getSender().getName() +" "+ e.getSender().getSurname());
        dto.setInstructorId(e.getSender().getId());
        dto.setReceiverName(e.getReceiver().getName());
        dto.setSectionId(e.getSection().getSectionId());
        dto.setSectionCode(e.getSection().getSectionCode());
        dto.setTaNeeded(e.getTaNeeded());
        dto.setAmountOfAssignedTas(e.getAmountOfAssignedTas());
        String[] parts = e.getSection().getSectionCode().split("-");
        dto.setCourseCode(parts[0] +"-"+ parts[1]);
        dto.setPreferredTas(e.getPreferredTas().stream()
            .map(t -> {
                TaInfo ti = new TaInfo();
                ti.setId(t.getId());
                ti.setName(t.getName());
                ti.setSurname(t.getSurname());
                return ti;
            })
            .collect(Collectors.toList())
        );

        dto.setNonPreferredTas(e.getNonPreferredTas().stream()
            .map(t -> {
                TaInfo ti = new TaInfo();
                ti.setId(t.getId());
                ti.setName(t.getName());
                ti.setSurname(t.getSurname());
                return ti;
            })
            .collect(Collectors.toList())
        );

        return dto;
    }

    private LeaveDTO toDto(Leave e) {
        LeaveDTO dto = new LeaveDTO();
        dto.setCourseCode(e.getCourseCode());
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setRequestType(e.getRequestType());
        dto.setPending(e.isPending());
        dto.setSenderId(e.getSender().getId());
        dto.setDepName(e.getReceiver().getName());
        dto.setDuration(e.getDuration());
        dto.setAttachmentFilename(e.getAttachmentFilename());
        dto.setAttachmentContentType(e.getAttachmentContentType());
        dto.setSenderName(e.getSender().getName() + " " + e.getSender().getSurname());
        dto.setReceiverName(e.getReceiver().getName());
        dto.setTasks(taTaskRepo.findTasksForTaInInterval(e.getSender().getId(), dto.getDuration().getStart(), dto.getDuration().getFinish()).stream()
                    .map(task -> {
                        TaskDto taskDto = new TaskDto();
                        taskDto.setDuration(task.getDuration());
                        taskDto.setType(task.getTaskType().toString());
                        return taskDto;
                    })
                    .toList());
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/requests/")
            .path(e.getRequestId().toString())
            .path("/attachment")
            .toUriString();
        dto.setAttachmentUrl(url);
        return dto;
    }

    @Override
    public List<ProctorTaInFacultyDto> getReceivedProctoTaInFacultyOfTheDean(Long deanOfficeId) {
        DeanOffice dean = deanOfficeRepo.findById(deanOfficeId)
            .orElseThrow(() -> new NoSuchElementException(
                "DeanOffice not found: " + deanOfficeId
            ));

        return proctorTaInFacultyRepo.findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(deanOfficeId)
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
    }

    @Override
    public List<ProctorTaFromOtherFacultyDto> getReceivedProctorTaFromOtherFacOfTheDean(Long deanOfficeId) {
        DeanOffice dean = deanOfficeRepo.findById(deanOfficeId)
            .orElseThrow(() -> new NoSuchElementException(
                "DeanOffice not found: " + deanOfficeId
            ));

        // 2) fetch all pending ProctorTaFromOtherFaculty for that receiver
        return prTaFrOtherFacRepo.findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(
                deanOfficeId).stream()
                   .map(this::toDto)
                   .collect(Collectors.toList());
    }

    @Override
    public List<WorkLoadDto> getReceivedWorkLoadRequestsOfTheInstr(Long instructorId) {
        Instructor instr = instructorRepo.findById(instructorId)
                 .orElseThrow(() -> new NoSuchElementException(
                     "Instructor not found: " + instructorId
                 ));

        return workloadRepo
        .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(instructorId)
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
    }

    @Override
    public List<TransferProctoringDto> getReceivedProctoringRequestsOfTheTa(Long taId) {
        TA ta = taRepo.findById(taId)
                      .orElseThrow(() -> new NoSuchElementException("TA not found: " + taId));
        return transRepo
        .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(taId)
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
    }

    @Override
    public List<SwapDto> getReceivedSwapProctoringRequestsOfTheTa(Long taId) {
        TA ta = taRepo.findById(taId)
                      .orElseThrow(() -> new NoSuchElementException("TA not found: " + taId));


        // 1) Swap requests received by this TA
        return swapRepo
        .findByReceiver_IdAndIsPendingTrueAndIsApprovedFalseAndIsRejectedFalse(taId)
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
    }

    private ProctorTaFromOtherFacultyDto toDto(ProctorTaFromOtherFaculty e) {
        ProctorTaFromOtherFacultyDto dto = new ProctorTaFromOtherFacultyDto();
        dto.setCourseCode(e.getCourseCode());
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setRequestType(e.getRequestType());
        dto.setPending(e.isPending());
        dto.setDescription(e.getExam().getDescription());
        dto.setSenderId(e.getSender().getId());
        dto.setReceiverId(e.getReceiver().getId());
        dto.setSenderName(e.getSender().getName());
        dto.setReceiverName(e.getReceiver().getName());
        dto.setExamId(e.getExam().getExamId());
        dto.setExamName(e.getExam().getDescription());
        return dto;
    }

    private WorkLoadDto toDto(WorkLoad e) {
        WorkLoadDto dto = new WorkLoadDto();
        dto.setCourseCode(e.getCourseCode());
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setRequestType(e.getRequestType());
        dto.setPending(e.isPending());

        dto.setReceiverId(e.getReceiver().getId());
        dto.setSenderId(e.getSender().getId());
        dto.setTaskId(e.getTask().getTaskId());
        dto.setSenderName(e.getSender().getName() + " " + e.getSender().getSurname());
        dto.setReceiverName(e.getReceiver().getName() + " " + e.getReceiver().getSurname());
        dto.setTaskType(e.getTask().getTaskType().name());
        dto.setDuration(e.getTask().getDuration());
        dto.setWorkload(e.getTask().getWorkload());
        return dto;
    }

    private SwapDto toDto(Swap e) {
        SwapDto dto = new SwapDto();
        dto.setCourseCode(e.getCourseCode());
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setRequestType(e.getRequestType());
        dto.setPending(e.isPending());

        dto.setReceiverId(e.getReceiver().getId());
        dto.setSenderId(e.getSender().getId());
        dto.setDescription("TA with id " + e.getSender().getId() + " wants to swap his proctoring with the TA with id " + e.getReceiver().getId());
        dto.setSenderName(e.getSender().getName() + " " + e.getSender().getSurname());
        dto.setReceiverName(e.getReceiver().getName() + " " + e.getReceiver().getSurname());
        dto.setSenderExamId(e.getSendersExam().getExamId());
        dto.setReceiverExamId(e.getReceiversExam().getExamId());
        dto.setSenderExamName(e.getSendersExam().getDescription());
        dto.setReceiverExamName(e.getReceiversExam().getDescription());
        return dto;
    }

        private TransferProctoringDto toDto(TransferProctoring e) {
        TransferProctoringDto dto = new TransferProctoringDto();
        dto.setCourseCode(e.getCourseCode());
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setRequestType(e.getRequestType());
        dto.setPending(e.isPending());

        dto.setDescription(e.getExam().getDescription());
        dto.setSenderId(e.getSender().getId());
        dto.setReceiverId(e.getReceiver().getId());
        dto.setDuration(e.getExam().getDuration());
        dto.setExamId(e.getExam().getExamId());
        dto.setExamName(e.getExam().getDescription());
        return dto;
    }

    private ProctorTaInFacultyDto toDto(ProctorTaInFaculty req) {
    ProctorTaInFacultyDto dto = new ProctorTaInFacultyDto();
    dto.setCourseCode(req.getCourseCode());
    dto.setRequestId(req.getRequestId());
    dto.setRequestType(req.getRequestType());
    dto.setDescription(req.getDescription());
    dto.setSenderName(req.getSender().getName());
    dto.setReceiverName(req.getReceiver().getName());
    dto.setSentTime(req.getSentTime());
    dto.setApproved(req.isApproved());
    dto.setRejected(req.isRejected());
    dto.setPending(req.isPending());
    dto.setFacultyName(req.getSender().getFaculty().getCode()); 
    dto.setDean_id(req.getReceiver().getId() );
    dto.setInstrId(req.getInstrId());
    dto.setExamName(req.getExam().getDescription());
    dto.setExamId(req.getExam().getExamId());
    dto.setRequiredTas(req.getRequiredTas());
    dto.setTasLeft(req.getTasLeft());

    return dto;
}
}
