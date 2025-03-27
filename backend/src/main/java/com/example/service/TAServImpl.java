package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.entity.TA;
import com.example.entity.Task;
import com.example.exception.UserNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.TaskRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TAServImpl implements TAServ {

    private final TARepo repo;
    private final TaskRepo taskRepo;

    @Override
    public TA getTAById(Long id){
        return repo.findById(id)
                .orElseThrow(() -> new UserNotFoundExc(id));
    }

    @Override
    public List<TA> getAllTAs() {
        return repo.findAllTAs(); // Use the custom query to fetch all TAs
    }

    @Override
    public Task getTaskById(int id) {
        return taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task with ID " + id + " not found."));
    }

    @Override
    public List<Task> getAllTasks(Long id) {
        return taskRepo.findAll();
    }

    @Override
    public Task createTask(Task task) {
        if (task.getTas_list() == null || task.getTas_list().isEmpty()) {
            throw new RuntimeException("Task must be assigned to at least one TA.");
        }

        // Validate that all assigned TAs exist
        for (TA ta : task.getTas_list()) {
            if (!repo.existsById(ta.getId())) {
                throw new RuntimeException("TA with ID " + ta.getId() + " does not exist.");
            }
        }

        return taskRepo.save(task);
    }
    
    @Override
    public boolean deleteTaskById(int id) {
        Optional<Task> taskOptional = taskRepo.findById(id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + id + " not found.");
        }
        taskRepo.deleteById(id);
        return true;
    }

    @Override
    public boolean deleteTAById(Long id)
    {
        Optional<TA> taOpt = repo.findById(id);
        if (taOpt.isEmpty()) {
            throw new UserNotFoundExc(id);
        }
        TA ta = taOpt.get();
        ta.delete();
        repo.save(ta);
        return true;
    }
}
