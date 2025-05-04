package com.example.mapper;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.dto.TaskDto;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromFacultiesDto;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.example.entity.Requests.ProctorTaInFacultyDto;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.RequestDto;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.SwapDto;
import com.example.entity.Requests.SwapEnable;
import com.example.entity.Requests.SwapEnableDto;
import com.example.entity.Requests.TransferProctoring;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.entity.Requests.WorkLoad;
import com.example.entity.Requests.WorkLoadDto;
import com.example.repo.TaTaskRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestMapper {

    private final TaTaskRepo taTaskRepo;

    public RequestDto toDto(Request req) {
        if (req instanceof Leave leave) {
            LeaveDTO dto = new LeaveDTO();
            BeanUtils.copyProperties(leave, dto);
            dto.setSenderName(leave.getSender().getName() + " " + leave.getSender().getSurname());
            dto.setReceiverName(leave.getReceiver().getName() + " " + leave.getReceiver().getSurname());
            dto.setTasks(taTaskRepo.findTasksForTaInInterval(leave.getSender().getId(), dto.getDuration().getStart(), dto.getDuration().getFinish()).stream()
                    .map(task -> {
                        TaskDto taskDto = new TaskDto();
                        taskDto.setDuration(task.getDuration().toString());
                        taskDto.setType(task.getTaskType().toString());
                        return taskDto;
                    })
                    .toList());

            // build absolute (or relative) URL to the download endpoint
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/requests/")
                .path(req.getRequestId().toString())
                .path("/attachment")
                .toUriString();

            dto.setAttachmentUrl(url);
            return dto;
        }
        else if (req instanceof Swap swap) {
            SwapDto dto = new SwapDto();
            BeanUtils.copyProperties(swap, dto);
            dto.setSenderName(swap.getSender().getName() + " " + swap.getSender().getSurname());
            dto.setReceiverName(swap.getReceiver().getName() + " " + swap.getReceiver().getSurname());
            dto.setExamName(swap.getExam().getDescription());
            dto.setExamId(swap.getExam().getExamId());
            dto.setDuration(swap.getExam().getTask().getDuration());
            return dto;
        }
        else if (req instanceof SwapEnable se) {
            SwapEnableDto dto = new SwapEnableDto();
            BeanUtils.copyProperties(se, dto);
            dto.setSenderName(se.getSender().getName() + " " + se.getSender().getSurname());
            dto.setReceiverName(se.getReceiver().getName() + " " + se.getReceiver().getSurname());
            dto.setDuration(se.getExam().getTask().getDuration());
            dto.setExamName(se.getExam().getDescription());
            dto.setExamId(se.getExam().getExamId());
            return dto;
        }
        else if (req instanceof TransferProctoring tp) {
            TransferProctoringDto dto = new TransferProctoringDto();
            BeanUtils.copyProperties(tp, dto);
            dto.setSenderName(tp.getSender().getName() + " " + tp.getSender().getSurname());
            dto.setReceiverName(tp.getReceiver().getName() + " " + tp.getReceiver().getSurname());
            dto.setDuration(tp.getExam().getTask().getDuration());
            dto.setExamId(tp.getExam().getExamId());
            dto.setExamName(tp.getExam().getDescription());
            return dto;
        }
        else if (req instanceof ProctorTaFromFaculties pf) {
            ProctorTaFromFacultiesDto dto = new ProctorTaFromFacultiesDto();
            BeanUtils.copyProperties(pf, dto);
            dto.setSenderName(pf.getSender().getName() + " " + pf.getSender().getSurname());
            dto.setReceiverName(pf.getReceiver().getName() + " " + pf.getReceiver().getSurname());

            // collect its child ProctorTaInFaculty by matching sentTime
            List<ProctorTaInFacultyDto> children = pf.getProctorTaInFaculties()
                .stream()
                .map(child -> {
                    ProctorTaInFacultyDto cdto = new ProctorTaInFacultyDto();
                    // copies Request‑inherited fields (id, type, description, sentTime, etc)
                    BeanUtils.copyProperties(child, cdto);
        
                    // now subclass‑specific properties:
                    cdto.setFacultyName(child.getFaculty().getCode());
                    cdto.setExamName  (child.getExam().getDescription());
                    cdto.setExamId    (child.getExam().getExamId());
                    // if your entity ProctorTaInFaculty has requiredTas:
                    // cdto.setRequiredTas(child.getRequiredTas());
        
                    return cdto;
                })
            .toList();

            dto.setProctorTaInFacultyDtos(children);
            return dto;
        }
        else if (req instanceof ProctorTaInFaculty pi) {
            ProctorTaInFacultyDto dto = new ProctorTaInFacultyDto();
            BeanUtils.copyProperties(pi, dto);
            dto.setSenderName(pi.getSender().getName() + " " + pi.getSender().getSurname());
            dto.setReceiverName(pi.getReceiver().getName() + " " + pi.getReceiver().getSurname());
            dto.setFacultyName(pi.getFaculty().getCode());
            dto.setExamName(pi.getExam().getDescription());
            dto.setExamId(pi.getExam().getExamId());
            return dto;
        }
        else if (req instanceof WorkLoad wl) {
            WorkLoadDto dto = new WorkLoadDto();
            BeanUtils.copyProperties(wl, dto);
            dto.setSenderName(wl.getSender().getName() + " " + wl.getSender().getSurname());
            dto.setReceiverName(wl.getReceiver().getName() + " " + wl.getReceiver().getSurname());
            dto.setTaskId(wl.getTask().getTaskId());
            dto.setDuration(wl.getTask().getDuration());
            dto.setTaskType(wl.getTask().getTaskType().toString());
            return dto;
        }
        else {
            throw new IllegalStateException("Unknown request type: " + req.getClass());
        }
    }
}
