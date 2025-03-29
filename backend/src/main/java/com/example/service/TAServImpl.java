package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.PrivateTask;
import com.example.entity.PublicTask;
import com.example.entity.TA;
import com.example.entity.Task;
import com.example.exception.UserNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.TaskRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TAServImpl implements TAServ {
    
    @Autowired
    private TARepo repo;

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private TaskServ taskServ;
    
    @Override
    public TA getTAById(Long id){
        return repo.findById(id)
        .orElseThrow(() -> new UserNotFoundExc(id));
    }
    
    @Override
    public List<TA> getAllTAs() {
        return repo.findAllTAs(); 
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTAById(Long id)
    {
        Optional<TA> taOpt = repo.findById(id);
        if (taOpt.isEmpty()) {
            throw new UserNotFoundExc(id);
        }
        TA ta = taOpt.get();
        delete(ta);
        repo.saveAndFlush(ta); 
        
        TA freshTa = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundExc(id));
        
        if (!freshTa.isDeleted()) {
            throw new IllegalStateException("Deletion did not persist");
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreTAById(Long id)
    {
        Optional<TA> taOpt = repo.findById(id);
        if (taOpt.isEmpty()) {
            throw new UserNotFoundExc(id);
        }
        TA ta = taOpt.get();
        restore(ta);
        repo.saveAndFlush(ta); 
        
        TA freshTa = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundExc(id));
        
        if (freshTa.isDeleted()) {
            throw new IllegalStateException("Restoration did not persist");
        }
        
        return true;
    }
    
    @Override
    public Task getTaskById(int task_id, Long ta_id) {
        Optional<TA> taOptional = repo.findById(ta_id);
        if (taOptional.isEmpty()) {
            throw new RuntimeException("TA with ID " + ta_id + " not found.");
        }  
        TA ta = taOptional.get();
        for (Task task : ta.getTa_public_tasks_list()) {
            if (task.getTask_id() == task_id) {
                return task;
            }
        }
        for (Task task : ta.getTa_private_tasks_list()) {
            if (task.getTask_id() == task_id) {
                return task;
            }
        }
        throw new RuntimeException("Task with ID " + task_id + " not found.");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task assignTask(Task task, Long id) {
        TA existingTA = repo.findById(id)
        .orElseThrow(() -> new RuntimeException("TA with ID " + id + " does not exist."));
        
        if (!taskServ.checkAndUpdateStatusTask(task)) {
            throw new RuntimeException("Task with ID " + task.getTask_id() + " is not active! Please check duration!"); // or can be warning that user set incorrect duration!!!!!
        }
        
        if (task instanceof PublicTask publicTask)
        {
            if(publicTask.getTas_list() == null) {
                publicTask.setTas_list(new HashSet<>());
            }
            publicTask.addTA();
            publicTask.getTas_list().add(existingTA);
            existingTA.getTa_public_tasks_list().add(publicTask);
            repo.saveAndFlush(existingTA);
            return publicTask;
        }
        else
        {
            PrivateTask privateTask = (PrivateTask) task;
            privateTask.setTa_owner(existingTA);
            existingTA.getTa_private_tasks_list().add(privateTask);
            repo.saveAndFlush(existingTA);
            return privateTask;
        }
    }
    
    @Override
    public boolean deleteTaskById(int task_id, Long ta_id) {
        Optional<TA> taOptional = repo.findById(ta_id);
        if (taOptional.isEmpty()) {
            throw new RuntimeException("TA with ID " + ta_id + " not found.");
        }
        TA ta = taOptional.get();
        for (PublicTask task : ta.getTa_public_tasks_list()) {
            if (task.getTask_id() == task_id) {
                ta.getTa_public_tasks_list().remove(task);
                task.getTas_list().remove(ta);
                repo.save(ta);
                taskRepo.save(task);
                return true;
            }
        }
        for (PrivateTask task : ta.getTa_private_tasks_list()) {
            if (task.getTask_id() == task_id) {
                ta.getTa_private_tasks_list().remove(task);
                task.setTa_owner(null);
                repo.save(ta);
                taskRepo.save(task);
                return true;
            }
        }
        throw new RuntimeException("Deletion failed!");
    }
    
    @Override
    public Set<Task> getAllTasks(Long id) {
        TA existingTA = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("TA with ID " + id + " does not exist."));
        Set<Task> tasks = new HashSet<>();
        tasks.addAll(existingTA.getTa_public_tasks_list());
        tasks.addAll(existingTA.getTa_private_tasks_list());
        return tasks;
    }

    private void mark_deleted(TA ta) {
        if (ta.isDeleted()) {
            throw new IllegalStateException("User already deleted");
        }
        ta.setDeleted(true);
        ta.getTa_public_tasks_list().clear();
        ta.getTa_private_tasks_list().clear();
    }

    private void delete(TA ta){
        mark_deleted(ta);
    }

    private void restore(TA ta) {
        if (!ta.isDeleted()) {
            throw new IllegalStateException("User is not deleted");
        }
        ta.setDeleted(false);
    }
}
