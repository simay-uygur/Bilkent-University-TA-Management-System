package com.example.mapper;


import org.springframework.stereotype.Component;

import com.example.dto.TaTaskDto;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;

@Component
public class TaTaskMapper {
    public TaTaskDto toDto(TaTask entity) {
        if (entity == null) {
            return null;
        }
        TaTaskDto dto = new TaTaskDto();
        // Map the access type of this TA assignment
        dto.setType(entity.getTask().getTaskType().toString());
        dto.setTaId(entity.getTaOwner().getId());
        // Derive a human-readable description (e.g., task type and section)
        Task task = entity.getTask();
        if (task != null) {
            dto.setTaskId(task.getTaskId());
            dto.setDescription(
                task.getTaskType() != null
                    ? task.getTaskType().name() + " (Section " + task.getSection().toString() + ")"
                    : null
            );
            dto.setDuration(task.getDuration());
            dto.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
            dto.setWorkload(task.getWorkload());
        }
        return dto;
    }
}
