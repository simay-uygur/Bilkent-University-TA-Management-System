package com.example.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;
import com.example.entity.Tasks.Task;
import org.springframework.web.multipart.MultipartFile;

public interface TAServ {
    public List<TA> getAllTAs();
    public boolean deleteTAById(Long id);
    public TA getTAById(Long id);
    public Task getTaskById(int task_id, Long ta_id);
    public Set<Task> getAllTasks(Long id);
    public boolean assignTask(Task task, Long id);
    public boolean deleteTaskById(int task_id, Long ta_id);
    public boolean restoreTAById(Long id);
    public Schedule getWeeklyScheduleForTA(TA ta, Date anyCustomDate);
    public List<ScheduleItem> getScheduleOfTheDay(TA ta, String day);
    public Map<String, Object> importTAsFromExcel(MultipartFile file) throws IOException;
}
