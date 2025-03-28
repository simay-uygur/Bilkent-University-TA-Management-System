package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        for (Task task : ta.getTa_tasks_list()) {
            if (task.getTask_id() == task_id) {
                return task;
            }
        }
        throw new RuntimeException("Task with ID " + task_id + " not found.");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task assignTask(Task task, Long id) {
        /*if (task.getTas_list() == null || task.getTas_list().isEmpty()) {
            throw new RuntimeException("Task must be assigned to at least one TA.");
        }

        TA existingTA = repo.findById(id)
        .orElseThrow(() -> new RuntimeException("TA with ID " + id + " does not exist."));
        
        existingTA.getTa_tasks_list().add(task);
        
        task.getTas_list().add(existingTA);
        repo.save(existingTA);
        return taskRepo.save(task);*/
        TA existingTA = repo.findById(id)
        .orElseThrow(() -> new RuntimeException("TA with ID " + id + " does not exist."));
    
        if(task.getTas_list() == null) {
            task.setTas_list(new HashSet<>());
        }
        
        //task.getTas_list().add(existingTA);
        
        existingTA.getTa_tasks_list().add(task);
        
        repo.save(existingTA);
        for (Task tas : existingTA.getTa_tasks_list()) {
            if (tas.getTask_id() == task.getTask_id()) {
                return tas;
            }
        }
        throw new RuntimeException("Task with ID " + task.getTask_id() + " not found.");
    }
    
    @Override
    public boolean deleteTaskById(int task_id, Long ta_id) {
        Optional<TA> taOptional = repo.findById(ta_id);
        if (taOptional.isEmpty()) {
            throw new RuntimeException("TA with ID " + ta_id + " not found.");
        }
        TA ta = taOptional.get();
        for (Task task : ta.getTa_tasks_list()) {
            if (task.getTask_id() == task_id) {
                ta.getTa_tasks_list().remove(task);
                task.getTas_list().remove(ta);
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
        return existingTA.getTa_tasks_list();
    }

    private void mark_deleted(TA ta) {
        if (ta.isDeleted()) {
            throw new IllegalStateException("User already deleted");
        }
        ta.setDeleted(true);
        ta.getTa_tasks_list().clear();
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
