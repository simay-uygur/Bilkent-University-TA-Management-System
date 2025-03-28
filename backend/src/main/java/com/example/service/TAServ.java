package com.example.service;

import java.util.List;
import java.util.Set;

import com.example.entity.TA;
import com.example.entity.Task;

public interface TAServ {
    public List<TA> getAllTAs();
    public boolean deleteTAById(Long id);
    public TA getTAById(Long id);
    public Task getTaskById(int task_id, Long ta_id);
    public Set<Task> getAllTasks(Long id);
    public Task assignTask(Task task, Long id);
    public boolean deleteTaskById(int task_id, Long ta_id);
    public boolean restoreTAById(Long id);
}
