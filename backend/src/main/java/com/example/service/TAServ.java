package com.example.service;

import java.util.List;

import com.example.entity.TA;
import com.example.entity.Task;

public interface TAServ {
    public List<TA> getAllTAs();
    public boolean deleteTAById(Long id);
    public TA getTAById(Long id);
    public Task getTaskById(int id);
    public List<Task> getAllTasks(Long id);
    public Task createTask(Task task);
    public boolean deleteTaskById(int id);
}
