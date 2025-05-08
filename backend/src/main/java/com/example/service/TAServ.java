package com.example.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.dto.TaDto;
import com.example.dto.TaTaskDto;
import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;
import com.example.entity.Tasks.Task;

public interface TAServ {
    public List<TaDto> getAllTAs();
    public boolean deleteTAById(Long id);

    TA getTAByIdTa(Long id);

    public TA getTAByIdEntity(Long id);
    public TaDto getTAByIdDto(Long id);
    public TaTaskDto getTaskById(int task_id, Long ta_id);
    public List<TaTaskDto> getAllTasTasks(Long id);
    public boolean assignTask(Task task, Long id);
    public boolean deleteTaskById(int task_id, Long ta_id);
    public boolean restoreTAById(Long id);
    public Schedule getWeeklyScheduleForTA(TA ta, Date anyCustomDate);
    public List<ScheduleItem> getScheduleOfTheDay(TA ta, String date);
    public Map<String, Object> importTAsFromExcel(MultipartFile file) throws IOException;
    public List<TaDto> getTAsByDepartment(String deptName);
    public List<TaDto> getTAsBySectionCode(String sectionCode);
}
