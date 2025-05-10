package com.example.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.dto.RequestDto;
import com.example.dto.TaskDto;
import com.example.entity.Requests.Leave;
import com.example.entity.Requests.LeaveDTO;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromFacultiesDto;
import com.example.entity.Requests.ProctorTaFromOtherFaculty;
import com.example.entity.Requests.ProctorTaFromOtherFacultyDto;
import com.example.entity.Requests.ProctorTaInDepartment;
import com.example.entity.Requests.ProctorTaInDepartmentDto;
import com.example.entity.Requests.Request;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.SwapDto;
import com.example.entity.Requests.TransferProctoring;
import com.example.entity.Requests.TransferProctoringDto;
import com.example.entity.Requests.WorkLoad;
import com.example.entity.Requests.WorkLoadDto;
import com.example.repo.DepartmentRepo;
import com.example.repo.ExamRepo;
import com.example.repo.InstructorRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;
import com.example.repo.TaskRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestMapper {

    private final TARepo taRepo;
    private final ExamRepo examRepo;
    private final TaskRepo taskRepo;
    private final InstructorRepo instrRepo;
    private final DepartmentRepo deptRepo;
    private final TaTaskRepo taTaskRepo;

    /**
     * Generic dispatcher to convert any Request subtype to its DTO.
     */
    public RequestDto toDto(Request r) {
        if (r instanceof Swap) {
            return toDto((Swap) r);
        } else if (r instanceof TransferProctoring) {
            return toDto((TransferProctoring) r);
        } else if (r instanceof WorkLoad) {
            return toDto((WorkLoad) r);
        } else if (r instanceof Leave) {
            return toDto((Leave) r);
        } else if (r instanceof ProctorTaFromFaculties) {
            return toDto((ProctorTaFromFaculties) r);
        } else if (r instanceof ProctorTaFromOtherFaculty) {
            return toDto((ProctorTaFromOtherFaculty) r);
        } else if (r instanceof ProctorTaInDepartment) {
            return toDto((ProctorTaInDepartment) r);
        }
        throw new IllegalArgumentException("Unknown Request type: " + r.getClass());
    }

    private SwapDto toDto(Swap e) {
        SwapDto dto = new SwapDto();
        copyBase(e, dto);
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
        copyBase(e, dto);
        dto.setDescription(e.getExam().getDescription());
        dto.setSenderId(e.getSender().getId());
        dto.setReceiverId(e.getReceiver().getId());
        dto.setDuration(e.getExam().getDuration());
        dto.setExamId(e.getExam().getExamId());
        dto.setExamName(e.getExam().getDescription());
        return dto;
    }

    private WorkLoadDto toDto(WorkLoad e) {
        WorkLoadDto dto = new WorkLoadDto();
        copyBase(e, dto);
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

    private LeaveDTO toDto(Leave e) {
        LeaveDTO dto = new LeaveDTO();
        copyBase(e, dto);
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

    private ProctorTaFromFacultiesDto toDto(ProctorTaFromFaculties e) {
        ProctorTaFromFacultiesDto dto = new ProctorTaFromFacultiesDto();
        copyBase(e, dto);
        dto.setDescription(e.getExam().getDescription());
        dto.setDeansId(e.getSender().getId());
        dto.setExamId(e.getExam().getExamId());
        dto.setExamName(e.getExam().getDescription());
        dto.setSenderName(e.getSender().getName());
        dto.setRequiredTas(e.getRequiredTas());
        dto.setTasLeft(e.getTasLeft());
        dto.setProctorTaInFacultyDtos(
            e.getProctorTaFromOtherFacs().stream()
             .map(this::toDto)
             .collect(Collectors.toList())
        );
        return dto;
    }

    private ProctorTaFromOtherFacultyDto toDto(ProctorTaFromOtherFaculty e) {
        ProctorTaFromOtherFacultyDto dto = new ProctorTaFromOtherFacultyDto();
        copyBase(e, dto);
        dto.setDescription(e.getExam().getDescription());
        dto.setSenderId(e.getSender().getId());
        dto.setReceiverId(e.getReceiver().getId());
        dto.setSenderName(e.getSender().getName());
        dto.setReceiverName(e.getReceiver().getName());
        dto.setExamId(e.getExam().getExamId());
        dto.setExamName(e.getExam().getDescription());
        return dto;
    }

    private ProctorTaInDepartmentDto toDto(ProctorTaInDepartment e) {
        ProctorTaInDepartmentDto dto = new ProctorTaInDepartmentDto();
        copyBase(e, dto);
        dto.setDescription(e.getExam().getDescription());
        dto.setDepName(e.getReceiver().getName());
        dto.setInstrId(e.getSender().getId());
        dto.setExamId(e.getExam().getExamId());
        dto.setExamName(e.getExam().getDescription());
        dto.setRequiredTas(e.getRequiredTas());
        dto.setTasLeft(e.getTasLeft());
        return dto;
    }

    // common fields
    private void copyBase(Request e, RequestDto dto) {
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setRequestType(e.getRequestType());
        dto.setPending(e.isPending());
    }
}
