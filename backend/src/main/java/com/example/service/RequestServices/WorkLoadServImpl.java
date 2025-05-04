package com.example.service.RequestServices;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.Actors.Role;
import com.example.entity.Actors.User;
import com.example.entity.General.Date;
import com.example.entity.Requests.WorkLoad;
import com.example.entity.Requests.WorkLoadDto;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.UserRepo;
import com.example.repo.RequestRepos.WorkLoadRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkLoadServImpl implements WorkLoadServ {

    private final UserRepo userRepo;
    private final WorkLoadRepo workLoadRepo;
    private final TARepo taRepo;

    @Override
    public void createWorkLoad(WorkLoadDto dto, Long senderId) {
        User sender = userRepo.findById(senderId)
        .orElseThrow(() -> new UserNotFoundExc(senderId));
        User receiver = userRepo.findById(dto.getReceiverId())
        .orElseThrow(() -> new UserNotFoundExc(dto.getReceiverId()));
        
        if (receiver.getRole() != Role.DEPARTMENT_STAFF) {
            throw new GeneralExc("Receiver must be a staff member.");
        }
        WorkLoad workloadReq = new WorkLoad();
        
        Date sent_time = new Date().currenDate();
        workloadReq.setSentTime(sent_time);
        workloadReq.setRequestType(dto.getRequestType());
        workloadReq.setDescription(dto.getDescription());
        workloadReq.setDuration(dto.);
        workloadReq.setSender(sender);
        workloadReq.setReceiver(receiver);
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
    
}
