package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.TA;
import com.example.entity.Task;
import com.example.entity.TaskState;
import com.example.repo.TaskRepo;

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

    @Override
    public boolean updateTask(Task task) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTask'");
    }

    @Override
    public TA getTAById(int task_id, Long ta_id) {
        Task task = taskRepo.findById(task_id)
                .orElseThrow(() -> new RuntimeException("Task with ID " + task_id + " not found."));

        if (task.getTas_list() == null) {
            throw new RuntimeException("Task with ID " + task_id + " has no TAs assigned.");
        }

        for (TA ta : task.getTas_list()) {
            if (ta.getId().equals(ta_id)) {
                return ta;
            }
        }
        throw new RuntimeException("TA with ID " + ta_id + " not found for task with ID " + task_id);
    }

    @Override
    public boolean assignTA(int task_id, TA ta) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }

        Task task = taskOptional.get();
        if (task.getAmount_of_tas() == task.getRequiredTAs()) {
            throw new RuntimeException("Task with ID " + task_id + " has reached the maximum number of TAs.");
        }

        task.getTas_list().add(ta);
        task.setAmount_of_tas(task.getAmount_of_tas() + 1);
        taskRepo.saveAndFlush(task);

        Optional<Task> taskOptional2 = taskRepo.findById(task_id);
        if (taskOptional2.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }
        Task t = taskOptional2.get();
        if (!t.getTas_list().contains(ta)) {
            throw new IllegalStateException("Assignment did not persist");
        }

        return true;
    }

    @Override
    public boolean unassignTA(int task_id, TA ta) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }

        Task task = taskOptional.get();
        if (task.getAmount_of_tas() == 0) {
            throw new RuntimeException("Task with ID " + task_id + " has no TAs assigned.");
        }

        task.getTas_list().remove(ta);
        task.setAmount_of_tas(task.getAmount_of_tas() - 1);
        taskRepo.saveAndFlush(task);

        Optional<Task> taskOptional2 = taskRepo.findById(task_id);
        if (taskOptional2.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }
        Task t = taskOptional2.get();
        if (!t.getTas_list().contains(ta)) {
            throw new IllegalStateException("Unassignment did not persist");
        }
        
        return true;
    }

    @Override
    public List<TA> getTAsByTaskId(int task_id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTAsByTaskId'");
    }

    @Override
    public boolean approveTask(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }
        Task task = taskOptional.get();
        approve(task);
        taskRepo.saveAndFlush(task);

        Task t = taskRepo.findById(task_id)
                .orElseThrow(() -> new RuntimeException("Task with ID " + task_id + " not found."));
        if (!t.getStatus().equals(TaskState.APPROVED)) {
            throw new IllegalStateException("Approval did not persist");
        }
    
        return true;
    }

    @Override
    public boolean rejectTask(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }
        Task task = taskOptional.get();
        reject(task);
        taskRepo.saveAndFlush(task);

        Task t = taskRepo.findById(task_id)
                .orElseThrow(() -> new RuntimeException("Task with ID " + task_id + " not found."));
        if (!t.getStatus().equals(TaskState.APPROVED)) {
            throw new IllegalStateException("Approval did not persist");
        }
    
        return true;
    }

    @Override
    public boolean checkTask(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }
        Task task = taskOptional.get();
        return task.getStatus().equals(TaskState.PENDING) && task.isTaskActive();
    }

    @Override
    public HashSet<Task> getApprovedTasks() {
        return taskRepo.findApprovedTasks();
    }

    @Override
    public HashSet<Task> getPendingTasks() {
        return taskRepo.findPendingTasks();
    }

    @Override
    public HashSet<Task> getRejectedTasks() {
        return taskRepo.findRejectedTasks();
    }

    @Override
    public HashSet<Task> getDeletedTasks() {
        return taskRepo.findDeletedTasks();
    }

    private void mark_approved(Task t) {
        t.setStatus(TaskState.APPROVED);
        for (TA ta : t.getTas_list()) {
            ta.getTa_tasks_list().remove(t);
        }
        //tas_list.clear();
    }

    private void approve(Task t){
        mark_approved(t);
    }

    private void mark_rejected(Task t) {
        t.setStatus(TaskState.REJECTED);
        for (TA ta : t.getTas_list()) {
            ta.getTa_tasks_list().remove(t);
        }
        //tas_list.clear();
    }

    private void reject(Task t){
        mark_rejected(t);
    }
}