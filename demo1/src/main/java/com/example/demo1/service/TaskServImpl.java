package com.example.demo1.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo1.entity.Task;
import com.example.demo1.repo.TaskRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class TaskServImpl implements TaskServ {

    @Autowired
    private TaskRepo taskRepo;

    @Override
    public boolean createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        taskRepo.save(task);
        return true;
    }

    @Override
    public boolean deleteTask(int id) {
        Optional<Task> taskOptional = taskRepo.findById(id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + id + " not found.");
        }
        taskRepo.deleteById(id);
        return true;
    }

    @Override
    public Task getTaskById(int id) {
        return taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task with ID " + id + " not found."));
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }
}