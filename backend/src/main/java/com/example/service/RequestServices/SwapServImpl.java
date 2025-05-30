package com.example.service.RequestServices;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.SwapOptionDto;
import com.example.entity.Actors.TA;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.SwapDto;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.ExamRepo;
import com.example.repo.RequestRepos.SwapRepo;
import com.example.repo.TARepo;
import com.example.repo.UserRepo;
import com.example.service.LogService;
import com.example.service.MailService;
import com.example.service.NotificationService;
import com.example.service.TaskServ;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SwapServImpl implements SwapServ{

    private final SwapRepo swapRepo;
    private final UserRepo userRepo;
    private final ExamRepo examRepo;
    private final TARepo taRepo;
    private final TaskServ taskServ;
    private final LogService log;
    private final NotificationService notServ;
    private final MailService mailServ;
    @Async("setExecutor")
    @Override
    @Transactional
    public CompletableFuture<Boolean> createSwapRequest(SwapDto dto, Long senderId) {
        if (swapRepo.
        existsBySender_IdAndReceiver_IdAndSendersExam_ExamIdAndReceiversExam_ExamIdAndIsRejectedFalse
        (senderId, dto.getReceiverId(), dto.getSenderExamId(), dto.getReceiverExamId()))
        {
            throw new GeneralExc("Request already sent");
        }
        TA sender = taRepo.findById(senderId)
            .orElseThrow(() -> new UserNotFoundExc(senderId));
        TA receiver = taRepo.findById(dto.getReceiverId())
            .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));

        // lookup your Exam however you’ve defined in ExamRepo
        Exam senderExam = examRepo.findById(dto.getSenderExamId())
            .orElseThrow(() -> new RuntimeException("Exam not found: " + dto.getSenderExamName()));
        Exam receiverExam = examRepo.findById(dto.getReceiverExamId())
        .orElseThrow(() -> new RuntimeException("Exam not found: " + dto.getReceiverExamName()));
        Swap req = new Swap(senderExam, receiverExam);
        req.setRequestType(dto.getRequestType());
        req.setDescription(dto.getDescription());
        req.setSentTime(new Date().currenDate());
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setSendersExam(senderExam);
        req.setReceiversExam(receiverExam);
        req.setCourseCode(dto.getCourseCode());
        //req.setExam(exam);
        log.info("Swap Request Creation","TA with id: " + senderId +" wants to swap proctoring with the another TA with id: " + dto.getReceiverId());
        swapRepo.saveAndFlush(req);
        notServ.notifyCreation(req);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    @Transactional
    public boolean acceptSwapRequest(Long requestId, Long receiverId) {
        Swap req = swapRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such swap request with id "+ requestId));

        int senderExamWorkload = req.getSendersExam().getWorkload();
        int receiverExamWorkload = req.getReceiversExam().getWorkload();

        TA sender = req.getSender();
        TA receiver = req.getReceiver();

        /*if(taskServ.hasDutyOrLessonOrExam(sender, req.getReceiversExam().getDuration())){
            throw new GeneralExc("TA was assigned to another task");
        }

        if(taskServ.hasDutyOrLessonOrExam(receiver, req.getSendersExam().getDuration())){
            throw new GeneralExc("TA was assigned to another task");
        }*/

        int senderWorkload = sender.getTotalWorkload();
        int receiverWorkload = receiver.getTotalWorkload();
        sender.setTotalWorkload(senderWorkload - senderExamWorkload + receiverExamWorkload);
        receiver.setTotalWorkload(receiverWorkload - receiverExamWorkload + senderExamWorkload);

        Exam recExam = req.getReceiversExam();
        Exam senExam = req.getSendersExam();

        senExam.getAssignedTas().remove(sender);
        senExam.getAssignedTas().add(receiver);
        sender.getExams().remove(senExam);
        sender.getExams().add(recExam);

        recExam.getAssignedTas().remove(receiver);
        recExam.getAssignedTas().add(sender);
        receiver.getExams().remove(recExam);
        receiver.getExams().add(senExam);

        req.setApproved(true);
        req.setPending(false);
        log.info("Swap Request Approval","TA with id: " + receiverId +" accepted Swap Request with id: "+requestId);
        notServ.notifyApproval(req);
        String subject = "Swap Request #" + requestId + " Accepted";
        String bodyToSender = String.format(
            "Hello %s %s,\n\nYour swap request (ID=%d) to exchange “%s” (Exam #%d) with %s %s has been approved by %s %s.\n\n" +
            "Your new total workload is %d.\n\nRegards,\nTA Management System",
            sender.getName(), sender.getSurname(),
            requestId,
            senExam.getDescription(), senExam.getExamId(),
            receiver.getName(), receiver.getSurname(),
            receiver.getName(), receiver.getSurname(),
            sender.getTotalWorkload()
        );
        mailServ.send(sender.getWebmail(), subject, bodyToSender);

        String bodyToReceiver = String.format(
            "Hello %s %s,\n\nYou have successfully accepted the swap request (ID=%d) from %s %s.\n" +
            "They’ve taken over your assignment for “%s” (Exam #%d), and your new total workload is %d.\n\nRegards,\nTA Management System",
            receiver.getName(), receiver.getSurname(),
            requestId,
            sender.getName(), sender.getSurname(),
            recExam.getDescription(), recExam.getExamId(),
            receiver.getTotalWorkload()
        );
        mailServ.send(receiver.getWebmail(), subject, bodyToReceiver);
        return true;
    }

    @Override
    @Transactional
    public void rejectSwapRequest(Long requestId, Long receiverId) {
        Swap req = swapRepo.findById(requestId).orElseThrow(() -> new GeneralExc("There is no such swap request with id "+ requestId));
        req.setApproved(false);
        req.setRejected(true);
        req.setPending(false);
        log.info("Swap Request Rejection","TA with id: " + receiverId +" rejected Swap Request with id: "+requestId);
        swapRepo.save(req);
        notServ.notifyRejection(req);
    }

    @Override
    public void cancelSwapRequest(Long requestId, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelSwapRequest'");
    }

    @Override
    public void updateSwapRequest(SwapDto dto, Long requestId, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateSwapRequest'");
    }

    @Override
    public void getSwapRequestById(Long requestId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSwapRequestById'");
    }
    
    @Async("setExecutor")
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<SwapOptionDto>> findSwapCandidates(Long senderId, int senderExamId) {
        TA sender = taRepo.findById(senderId)
                          .orElseThrow(() -> new GeneralExc("No such sender TA"));
        Exam senderExam = examRepo.findById(senderExamId)
                                  .orElseThrow(() -> new GeneralExc("No such exam"));
        Event senderDur = senderExam.getDuration();

        return CompletableFuture.completedFuture(taRepo.findByDepartment(sender.getDepartment()).stream()
        .filter(candidate -> !candidate.getId().equals(senderId))
        .flatMap(candidate ->
            candidate.getExams().stream()  // each exam this TA currently proctors
                .filter(candidateExam -> {
                    Event candDur = candidateExam.getDuration();

                    // 1) candidate’s duty must NOT overlap sender’s (so candidate free at sender’s time)
                    boolean candFree = !candDur.has(senderDur);
                    // 2) sender must NOT have a conflict at candidate’s time
                    boolean senderFree = !senderDur.has(candDur);

                    return candFree && senderFree;
                })
                .map(candidateExam ->
                    new SwapOptionDto(
                        candidate.getId(),
                        candidate.getName() + " " + candidate.getSurname(), 
                        candidateExam.getExamId(),
                        candidateExam.getDuration()
                    )
                )
        )
        .collect(Collectors.toList()));
    }
}
