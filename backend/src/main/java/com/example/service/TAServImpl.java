package com.example.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;
import com.example.entity.Tasks.TA_Task;
import com.example.entity.Tasks.TA_TaskId;
import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskAccessType;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.exception.UserNotFoundExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.exception.taskExc.TaskIsNotActiveExc;
import com.example.exception.taskExc.TaskNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.TA_TaskRepo;
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

    @Autowired
    private TA_TaskRepo taTaskRepo;
    
    @Autowired
    private ScheduleServ scheduleServ;

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
            throw new NoPersistExc("Deletion");
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
            throw new NoPersistExc("Restoration");
        }
        
        return true;
    }
    
    @Override
    public Task getTaskById(int task_id, Long ta_id) {
        /*Optional<TA> taOptional = repo.findById(ta_id);
        if (taOptional.isEmpty()) {
            throw new TaNotFoundExc(ta_id);
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
        }*/
        TA_TaskId taTaskId = new TA_TaskId(task_id, ta_id);
        Optional<TA_Task> optTaTask = taTaskRepo.findById(taTaskId);
        TA_Task taTask = optTaTask.orElseThrow(() -> new TaskNotFoundExc(task_id));
        return taTask.getTask();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignTask(Task task, Long id, TaskAccessType type) {
        TA existingTA = repo.findById(id)
        .orElseThrow(() -> new TaNotFoundExc(id));
        
        if (!taskServ.checkAndUpdateStatusTask(task)) {
            throw new TaskIsNotActiveExc();
        }
        taskServ.assignTA(task.getTask_id(), existingTA, type) ;
        return true ;
    }
    
    @Override
    public boolean deleteTaskById(int task_id, Long ta_id) {
        Optional<TA> taOptional = repo.findById(ta_id);
        if (taOptional.isEmpty()) {
            throw new TaNotFoundExc(ta_id);
        }
        TA ta = taOptional.get();
        taskServ.unassignTA(task_id, ta) ; 
        return true ;
    }
    
    @Override
    public Set<Task> getAllTasks(Long id) {
        TA existingTA = repo.findById(id)
            .orElseThrow(() -> new TaNotFoundExc(id));
        List<TA_Task> ta_tasks = taTaskRepo.findAllByTaId(id);
        Set<Task> tasks_list = new HashSet<>();
        for (TA_Task ta_task : ta_tasks) {
            Task task = ta_task.getTask();
            if (task != null) {
                tasks_list.add(task);
            }
        }
        return tasks_list;
    }

    private void mark_deleted(TA ta) {
        if (ta.isDeleted()) {
            throw new GeneralExc("User already deleted");
        }
        ta.setDeleted(true);
        List<TA_Task> ta_tasks = taTaskRepo.findAllByTaId(ta.getId());
        for (TA_Task ta_task : ta_tasks) {
            Task task = ta_task.getTask();
            if (task != null) {
                taskServ.unassignTA(task.getTask_id(), ta);
            }
        }
        taTaskRepo.deleteAll(ta_tasks);
        taTaskRepo.flush();
        repo.saveAndFlush(ta);
    }

    private void delete(TA ta){
        mark_deleted(ta);
    }

    private void restore(TA ta) {
        if (!ta.isDeleted()) {
            throw new GeneralExc("User is not deleted");
        }
        ta.setDeleted(false);
    }

    @Override
    public Schedule getWeeklyScheduleForTA(TA ta, Date anyCustomDate) {
        if (ta == null) {
            throw new TaNotFoundExc(-1L);
        }
        ta.setSchedule(scheduleServ.getWeeklyScheduleForTA(ta, anyCustomDate));
        return ta.getSchedule();
    }

    @Override
    public List<ScheduleItem> getScheduleOfTheDay(TA ta, String day) {
        if (ta == null) {
            throw new TaNotFoundExc(-1L);
        }
        if (ta.getSchedule() != null)
            return ta.getSchedule().getDailySchedule().get(indexOf(day)).getScheduleItems() ;
        else 
        {
            Date date = new Date().currenDate();
            ta.setSchedule(scheduleServ.getWeeklyScheduleForTA(ta, date));
            return ta.getSchedule().getDailySchedule().get(indexOf(day)).getScheduleItems() ;
        }
    }

    private int indexOf(String day) {
        switch (day) {
            case "Monday":
                return 0;
            case "Tuesday":
                return 1;
            case "Wednesday":
                return 2;
            case "Thursday":
                return 3;
            case "Friday":
                return 4;
            case "Saturday":
                return 5;
            case "Sunday":
                return 6;
            default:
                throw new IllegalArgumentException("Invalid day: " + day);
        }
    }
}
