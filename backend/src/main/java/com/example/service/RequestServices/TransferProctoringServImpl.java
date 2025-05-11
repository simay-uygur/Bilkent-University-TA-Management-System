package com.example.service.RequestServices;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Actors.TA;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.Requests.TransferCandidateDto;
import com.example.entity.Requests.TransferProctoring;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.ExamRepo;
import com.example.repo.RequestRepos.TransferProctoringRepo;
import com.example.repo.TARepo;
import com.example.repo.UserRepo;
import com.example.service.LogService;
import com.example.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferProctoringServImpl implements TransferProctoringServ {

    private final UserRepo userRepo;
    private final TARepo taRepo;
    private final ExamRepo examRepo;
    private final TransferProctoringRepo transferRepo;
    private final LogService log;
    private final NotificationService notServ;
    @Async("setExecutor")
    @Override
    public void createTransferProctoringReq(TransferProctoringDto dto, Long senderId) {
        TA sender = taRepo.findById(senderId)
            .orElseThrow(() -> new UserNotFoundExc(senderId));
        TA receiver = taRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));
        
        
        Exam exam = examRepo.
        findById(dto.getExamId()).
        orElseThrow(() -> new GeneralExc("Exam not found: " + dto.getExamName()));

        TransferProctoring req = new TransferProctoring();
        req.setRequestType(dto.getRequestType());
        req.setDescription(dto.getDescription());
        req.setSentTime(new Date().currenDate());
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setExam(exam);
        req.setCourseCode(dto.getCourseCode());
        log.info("Transfer Proctoring Request Creation","TA with id: " + senderId +" wants to transfer his proctoring duty to another TA with id: " + dto.getReceiverId());
        transferRepo.save(req);
        notServ.notifyCreation(req);
    }

    @Override
    public void updateTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTransferProctoringReq'");
    }

    @Override
    public void deleteTransferProctoringReq(TransferProctoringDto transferProctoringDto, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteTransferProctoringReq'");
    }

    @Override
    public void approveRequest(Long requestId, Long receiverId)
    {
        TransferProctoring req = transferRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such swap request with id "+ requestId));

        int senderExamWorkload = req.getExam().getWorkload();

        TA sender = req.getSender();
        TA receiver = req.getReceiver();

        /*if(taskServ.hasDutyOrLessonOrExam(sender, req.getReceiversExam().getDuration())){
            throw new GeneralExc("TA was assigned to another task");
        }

        if(taskServ.hasDutyOrLessonOrExam(receiver, req.getSendersExam().getDuration())){
            throw new GeneralExc("TA was assigned to another task");
        }*/

        sender.decreaseWorkload(senderExamWorkload);
        receiver.increaseWorkload(senderExamWorkload);

        Exam senExam = req.getExam();

        senExam.getAssignedTas().remove(sender);
        senExam.getAssignedTas().add(receiver);
        sender.getExams().remove(senExam);
        receiver.getExams().add(senExam);

        req.setApproved(true);
        req.setPending(false);
        transferRepo.save(req);
        notServ.notifyApproval(req);
        log.info("Transfer Proctoring Request Approval","TA with id: " + receiverId +" accepted Transfer Proctoring Request with id: "+requestId);
    }
    @Override
    public void rejectRequest(Long requestId, Long receiverId){
        TransferProctoring req = transferRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such swap request with id "+ requestId));
        req.setApproved(false);
        req.setRejected(true);
        req.setPending(false);
        transferRepo.save(req);
        notServ.notifyRejection(req);
        log.info("Transfer Proctoring Request Rejection","TA with id: " + receiverId +" rejected Transfer Proctoring Request with id: "+requestId);
    }

    @Async("setExecutor")
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<TransferCandidateDto>> findTransferCandidates(Long senderId, int examId) {
        TA sender = taRepo.findById(senderId)
                        .orElseThrow(() -> new IllegalArgumentException("No such sender TA"));
        Exam exam = examRepo.findById(examId)
                            .orElseThrow(() -> new IllegalArgumentException("No such exam"));
        Event window = exam.getDuration();  

        return CompletableFuture.completedFuture(taRepo.findByDepartment(sender.getDepartment()).stream()

        .filter(candidate -> !candidate.getId().equals(senderId))

        .filter(candidate ->
            candidate.getExams().stream()
                    .noneMatch(e -> e.getDuration().has(window))
        )

        .filter(candidate ->
            candidate.getTaTasks().stream()
                    .map(tt -> tt.getTask().getDuration())
                    .noneMatch(d -> d.has(window))
        )
        .map(c -> new TransferCandidateDto(
            c.getId(),
            c.getName() + " " + c.getSurname()
        ))
        .collect(Collectors.toList()));
    }
}
