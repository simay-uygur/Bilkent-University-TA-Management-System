package com.example.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.dto.TaDto;
import com.example.dto.TaskDto;
import com.example.entity.General.Event;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskState;
import com.example.entity.Tasks.TaskType;

@Component
public class TaskMapper {

    private final TaMapper taMapper;

    public TaskMapper(TaMapper taMapper) {
        this.taMapper = taMapper;
    }

    /**
     * Convert Task entity to TaskDto.
     */
    public TaskDto toDto(Task task) {
        if (task == null) {
            return null;
        }
        TaskDto dto = new TaskDto();
        // map enum to its name
        dto.setType(task.getTaskType().name());
        // map list of TaTask → list of TaDto
        dto.setTas(
            task.getTasList().stream()
                .map(this::mapTaTaskToTaDto)
                .collect(Collectors.toList())
        );
        // you may need to add a description field to Task if you actually have one
        dto.setDescription(task.getSection().getSectionCode()); // example
        dto.setDuration(task.getDuration());
        dto.setStatus(task.getStatus().name());
        return dto;
    }

    /**
     * Convert TaskDto back to Task entity.
     * NOTE: this only sets simple fields; you’ll need to load
     * the Section, TaTask, etc. yourself in the service layer.
     */
    public Task toEntity(TaskDto dto) {
        if (dto == null) {
            return null;
        }
        Task task = new Task();
        task.setTaskType(TaskType.valueOf(dto.getType()));
        task.setDuration(dto.getDuration());
        task.setStatus(TaskState.valueOf(dto.getStatus()));
        // workload, amountOfTas, section, tasList, etc. should be set elsewhere
        return task;
    }

    private TaDto mapTaTaskToTaDto(TaTask taTask) {
        // assuming TaTask has a getTa() returning a TA entity
        return taMapper.toDto(taTask.getTaOwner());
    }
}
