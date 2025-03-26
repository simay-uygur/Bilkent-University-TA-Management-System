package com.example.demo1.service;

import java.util.List;

import com.example.demo1.entity.TA;
import com.example.demo1.entity.Task;

public interface TAServ {
    public List<TA> getAllTAs();
    public boolean deleteTAById(Long id);
    public TA getTAById(Long id);
    public Task getTaskById(int id);
    public List<Task> getAllTasks(Long id);
    public Task createTask(Task task);
    public boolean deleteTaskById(int id);
}
