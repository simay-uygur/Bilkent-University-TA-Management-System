package com.example.service;

import java.util.HashSet;
import java.util.List;

import com.example.entity.TA;
import com.example.entity.Task;

public interface TaskServ {
    public boolean createTask(Task task);
    public boolean deleteTask(int task_id);
    public Task getTaskById(int task_id);
    public List<Task> getAllTasks();
    public boolean updateTask(Task task);
    public TA getTAById(int task_id, Long ta_id);
    public boolean assignTA(int task_id, TA ta);
    public boolean unassignTA(int task_id, TA ta);
    public List<TA> getTAsByTaskId(int task_id);
    public boolean approveTask(int task_id);
    public boolean rejectTask(int task_id);
    public boolean checkTask(int task_id);
    public HashSet<Task> getApprovedTasks() ;
    public HashSet<Task> getPendingTasks() ;
    public HashSet<Task> getRejectedTasks() ;
    public HashSet<Task> getDeletedTasks() ;
}
