package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.dto.TaDto;
import com.example.dto.TaskDto;
import com.example.entity.Actors.TA;
import com.example.entity.Tasks.Task;

public interface TaskServ {
    public TaskDto createTask(TaskDto task, String section_code);
    public boolean soft_deleteTask(int task_id);
    public boolean strict_deleteTask(int task_id);
    public boolean restoreTask(int task_id);
    public TaskDto getTaskById(int task_id);
    public List<Task> getAllTasks();
    public boolean updateTask(int task_id, Task task);
    public List<TaDto> assignTasToTask(List<TaDto> tas, int taskId, int sectionNum, String courseCode, Long instrId);
    public TA getTAById(int task_id, Long ta_id);
    public boolean assignTA(Task task, TA ta, Long instr_id);
    public boolean unassignTA(Task task, TA ta, Long instr_id);
    public List<TaDto> getTAsByTaskId(int task_id);
    public boolean checkAndUpdateStatusTask(Task task);
    public HashSet<Task> getApprovedTasks() ;
    public List<Task> getPendingTasks() ;
    public HashSet<Task> getRejectedTasks() ;
    public HashSet<Task> getDeletedTasks() ;
    public boolean assignProctoring(int task_id, List<Long> ta_id);
    public CompletableFuture<List<TaDto>> getTasToAssignToTask(String courseCode, String sectionCode, int task_id, Long instrId);
    public boolean deleteTask(String section_code, int task_id);
    //public boolean assignTaToTask(String sectionCode, int taskId, Long taId);
    public boolean assignTasToTaskByTheirId(String sectionCode, int taskId, List<Long> tas);
   
}
