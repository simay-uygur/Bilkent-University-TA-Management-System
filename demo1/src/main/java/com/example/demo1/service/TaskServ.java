package com.example.demo1.service;

import java.util.List;

import com.example.demo1.entity.Task;

public interface TaskServ {
    public boolean createTask(Task task);
    public boolean deleteTask(int id);
    public Task getTaskById(int id);
    public List<Task> getAllTasks();
}
