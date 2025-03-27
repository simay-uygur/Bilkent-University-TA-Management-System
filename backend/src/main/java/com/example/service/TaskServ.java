package com.example.service;

import java.util.List;

import com.example.entity.Task;

public interface TaskServ {
    public boolean createTask(Task task);
    public boolean deleteTask(int id);
    public Task getTaskById(int id);
    public List<Task> getAllTasks();
}
