package com.example.service.RequestServices;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Requests.WorkLoad;
import com.example.entity.Requests.WorkLoadDto;
import com.example.entity.Tasks.TaTask;
import com.example.exception.GeneralExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.repo.InstructorRepo;
import com.example.repo.RequestRepos.WorkLoadRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;
import com.example.repo.UserRepo;
import com.example.service.LogService;
import com.example.service.NotificationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkLoadServImpl implements WorkLoadServ {

    private final UserRepo userRepo;
    private final WorkLoadRepo workLoadRepo;
    private final TARepo taRepo;
    private final TaTaskRepo taTaskRepo;
    private final InstructorRepo instrRepo;
    private final LogService log;
    private final NotificationService notServ;
    @Override
    public void createWorkLoad(WorkLoadDto dto, Long senderId) {
        TA ta = taRepo.findById(senderId).orElseThrow(() -> new TaNotFoundExc(senderId));
        Instructor instr = instrRepo.
        findById(dto.getReceiverId()).
        orElseThrow(() -> new GeneralExc("Instructor with id " + dto.getReceiverId() + " not found."));
        TaTask taTask = taTaskRepo.findByTaskIdAndTaId(dto.getTaskId(), senderId).
                        orElseThrow(() -> new GeneralExc("TA with id " + senderId + " does not have task with id " + dto.getTaskId()));
                    
        WorkLoad workloadReq = new WorkLoad();
        
        Date sent_time = new Date().currenDate();
        workloadReq.setSentTime(sent_time);
        workloadReq.setRequestType(dto.getRequestType());
        workloadReq.setDescription(dto.getDescription());
        workloadReq.setTask(taTask.getTask());
        workloadReq.setSender(ta);
        workloadReq.setReceiver(instr);
        workloadReq.setCourseCode(dto.getCourseCode());
        ta.getSendedWorkLoadRequests().add(workloadReq);
        instr.getReceivedWorkloadRequests().add(workloadReq);
        log.info("WorkLoad Request Creation","TA with id: "+senderId+" has sent WorkLoad Request for the task with id: "+dto.getTaskId()+" to Instructor with id: "+dto.getReceiverId());
        notServ.notifyCreation(workloadReq);
        workLoadRepo.save(workloadReq);
    }

    @Override
    public void updateWorkLoad(WorkLoadDto workLoadDto, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateWorkLoad'");
    }

    @Override
    public void deleteWorkLoad(Long id, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteWorkLoad'");
    }

    @Override
    public WorkLoadDto getWorkLoadById(Long id, Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWorkLoadById'");
    }

    @Override
    public List<WorkLoadDto> getAllWorkLoadBySenderId(Long senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllWorkLoadBySenderId'");
    }

    @Override
    public List<WorkLoadDto> getAllWorkLoadByReceiverId(Long receiverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllWorkLoadByReceiverId'");
    }

    @Override
    public List<WorkLoadDto> getAllWorkLoadBySenderIdAndReceiverId(Long senderId, Long receiverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllWorkLoadBySenderIdAndReceiverId'");
    }
    
    @Override
    @Transactional
    public boolean approveRequest(Long instrId, Long requestId){
        WorkLoad req = workLoadRepo.findById(requestId).orElseThrow(() -> new GeneralExc("No such request found!"));
        TA sender = req.getSender();
        sender.increaseWorkload(req.getTask().getWorkload());
        taRepo.save(sender);
        req.setApproved(true);
        req.setRejected(false);
        req.setPending(false);
        workLoadRepo.save(req);
        log.info("WorkLoad Request Approval","Instructor with id: " + instrId + " has accepted the WorkLoad Request for the Task with id: " +req.getTask().getTaskId());
        notServ.notifyApproval(req);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectRequest(Long instrId, Long requestId){
        WorkLoad req = workLoadRepo.findById(requestId).orElseThrow(() -> new GeneralExc("No such request found!"));
        req.setApproved(false);
        req.setRejected(true);
        req.setPending(false);
        workLoadRepo.save(req);
        log.info("WorkLoad Request Rejection","Instructor with id: " + instrId + " has rejected the WorkLoad Request for the Task with id: " +req.getTask().getTaskId());
        notServ.notifyRejection(req);
        return true;
    }
}
