package com.example.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

import com.example.dto.TaDto;
import com.example.dto.TaTaskDto;
import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Schedule.ScheduleItemDto;
import com.example.entity.Tasks.TaGradingDto;
import com.example.entity.Tasks.TaProctorDto;
import com.example.entity.Tasks.Task;

public interface TAServ {
    List<TaDto> getAllTAs();
    boolean deleteTAById(Long id);

    TA getTAByIdTa(Long id);

    TA getTAByIdEntity(Long id);
    TaDto getTAByIdDto(Long id);
    TaTaskDto getTaskById(int task_id, Long ta_id);
    List<TaTaskDto> getAllTasTasks(Long id);
    boolean assignTask(Task task, Long id);
    boolean deleteTaskById(int task_id, Long ta_id);
    boolean restoreTAById(Long id);
    List<ScheduleItemDto> getWeeklyScheduleForTA(Long id, Date anyCustomDate);
    Map<String, Object> importTAsFromExcel(MultipartFile file) throws IOException;
    List<TaDto> getTAsByDepartment(String deptName);
    CompletableFuture<List<TaProctorDto>> getAssignedExamsOfTa(Long taId);
    CompletableFuture<List<TaGradingDto>> getGradingsOfTheTa(Long taId);
}
