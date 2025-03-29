package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.PrivateTask;
import com.example.entity.PublicTask;
import com.example.entity.TA;
import com.example.entity.Task;
import com.example.entity.TaskState;
import com.example.repo.TARepo;
import com.example.repo.TaskRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class TaskServImpl implements TaskServ {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private TARepo taRepo;

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        checkAndUpdateStatusTask(task);
        return taskRepo.saveAndFlush(task);
    }

    @Override
    public boolean soft_deleteTask(int id) {
        Optional<Task> taskOptional = taskRepo.findById(id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + id + " not found.");
        }

        Task task = taskOptional.get();
        task.setStatus(TaskState.DELETED);
        taskRepo.saveAndFlush(task);
        Task t = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task with ID " + id + " not found."));
        if (!t.getStatus().equals(TaskState.DELETED)) {
            throw new IllegalStateException("Deletion did not persist");
        }
        return true;
    }

    @Override
    public boolean strict_deleteTask(int id) {
        Optional<Task> taskOptional = taskRepo.findById(id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + id + " not found.");
        }

        Task task = taskOptional.get();
        taskRepo.delete(task);
        return true;
    }

    @Override
    public boolean restoreTask(int id) {
        Optional<Task> taskOptional = taskRepo.findById(id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + id + " not found.");
        }

        Task task = taskOptional.get();
        checkAndUpdateStatusTask(task);
        taskRepo.saveAndFlush(task);
        Task t = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task with ID " + id + " not found."));
        if (t.getStatus().equals(TaskState.DELETED)) {
            throw new IllegalStateException("Restoration did not persist");
        }
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

        if (task instanceof PublicTask publicTask)
        {
            if (publicTask.getTas_list() == null) {
                throw new RuntimeException("Task with ID " + task_id + " has no TAs assigned.");
            }
            for (TA ta : publicTask.getTas_list()) {
                if (ta.getId().equals(ta_id)) {
                    return ta;
                }
            }
            throw new RuntimeException("TA with ID " + ta_id + " not found for task with ID " + task_id);
        }
        else 
        {
            PrivateTask privateTask = (PrivateTask) task;
            if (privateTask.getTa_owner() == null) {
                throw new RuntimeException("TA owner not found for task with ID " + task_id);
            }
            if (!privateTask.getTa_owner().getId().equals(ta_id)) {
                throw new RuntimeException("TA with ID " + ta_id + " is not the owner of task with ID " + task_id);
            }
            return privateTask.getTa_owner();
        }
    }

    @Override
    public boolean assignTA(int task_id, TA ta) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }

        PublicTask task = (PublicTask) taskOptional.get();
        if (task.getAmount_of_tas() == task.getRequiredTAs()) {
            throw new RuntimeException("Task with ID " + task_id + " has reached the maximum number of TAs.");
        }

        task.getTas_list().add(ta);
        task.addTA();
        ta.getTa_public_tasks_list().add(task);
        taRepo.saveAndFlush(ta);
        taskRepo.saveAndFlush(task);

        Optional<Task> taskOptional2 = taskRepo.findById(task_id);
        if (taskOptional2.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }
        PublicTask t = (PublicTask) taskOptional2.get();
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

        PublicTask task = (PublicTask) taskOptional.get();
        if (task.getAmount_of_tas() == 0) {
            throw new RuntimeException("Task with ID " + task_id + " has no TAs assigned.");
        }

        task.getTas_list().remove(ta);
        task.removeTA();
        ta.getTa_public_tasks_list().remove(task);
        taRepo.saveAndFlush(ta);
        taskRepo.saveAndFlush(task);

        Optional<Task> taskOptional2 = taskRepo.findById(task_id);
        if (taskOptional2.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }
        PublicTask t = (PublicTask) taskOptional2.get();
        if (t.getTas_list().contains(ta)) {
            throw new IllegalStateException("Unassignment did not persist");
        }
        
        return true;
    }

    @Override
    public Set<TA> getTAsByTaskId(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new RuntimeException("Task with ID " + task_id + " not found.");
        }

        PublicTask task = (PublicTask) taskOptional.get();
        if (task.getAmount_of_tas() == 0) {
            throw new RuntimeException("Task with ID " + task_id + " has no TAs assigned.");
        }
        System.out.println(task.getTas_list().isEmpty());
        return task.getTas_list() ;
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
        if (!t.getStatus().equals(TaskState.REJECTED)) {
            throw new IllegalStateException("Rejection did not persist");
        }
    
        return true;
    }

    @Override
    public boolean checkAndUpdateStatusTask(Task task) {
        if (task == null) {
            throw new RuntimeException("Task not found.");
        }
        if (task.isTaskActive())
        {
            mark_pending(task);
            taskRepo.saveAndFlush(task);
            return true;
        }
        mark_not_responded(task);
        task.setTimePassed(true);
        taskRepo.saveAndFlush(task);
        return true; // warning if ta forgot to enter task and he enters task with past duration and system sends warning to him if he wrote correct duration or not
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
        if (t instanceof PublicTask publicTask) 
        {
            for (TA ta : publicTask.getTas_list()) {
                ta.getTa_public_tasks_list().remove(publicTask);
            }
        } else 
        {
            PrivateTask privateTask = (PrivateTask) t;
            privateTask.getTa_owner().getTa_private_tasks_list().remove(privateTask);
        }
    }

    private void approve(Task t){
        mark_approved(t);
    }

    private void mark_rejected(Task t) {
        t.setStatus(TaskState.REJECTED);
        if (t instanceof PublicTask publicTask) 
        {
            for (TA ta : publicTask.getTas_list()) {
                ta.getTa_public_tasks_list().remove(publicTask);
            }
        } else 
        {
            PrivateTask privateTask = (PrivateTask) t;
            privateTask.getTa_owner().getTa_private_tasks_list().remove(privateTask);
        }
    }

    private void reject(Task t){
        mark_rejected(t);
    }

    private void mark_not_responded(Task t) {
        t.setStatus(TaskState.NORESPOND);
    }

    private void mark_pending(Task t) {
        t.setStatus(TaskState.PENDING);
    }
}