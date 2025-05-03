package com.example.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.TaTaskId;
import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskState;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.exception.taskExc.TaskLimitExc;
import com.example.exception.taskExc.TaskNoTasExc;
import com.example.exception.taskExc.TaskNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;
import com.example.repo.TaskRepo;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
public class TaskServImpl implements TaskServ {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private TARepo taRepo;

    @Autowired
    private TaTaskRepo taTaskRepo;

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new GeneralExc("Task cannot be null!");
        }
        checkAndUpdateStatusTask(task);
        return taskRepo.save(task);
    }

    @Override
    public boolean soft_deleteTask(int id) {
        Optional<Task> taskOptional = taskRepo.findById(id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(id);
        }

        Task task = taskOptional.get();
        task.setStatus(TaskState.DELETED);
        taskRepo.saveAndFlush(task);
        Task t = taskRepo.findById(id)
                .orElseThrow(() -> new TaskNotFoundExc(id));
        if (!t.getStatus().equals(TaskState.DELETED)) {
            throw new NoPersistExc("Deletion") ;
        }
        return true;
    }

    @Override
    public boolean strict_deleteTask(int id) {
        Optional<Task> taskOptional = taskRepo.findById(id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(id);
        }

        Task task = taskOptional.get();
        taskRepo.delete(task);
        return true;
    }

    @Override
    public boolean restoreTask(int id) {
        Optional<Task> taskOptional = taskRepo.findById(id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(id);
        }

        Task task = taskOptional.get();
        checkAndUpdateStatusTask(task);
        taskRepo.saveAndFlush(task);
        Task t = taskRepo.findById(id)
                .orElseThrow(() -> new TaskNotFoundExc(id));
        if (t.getStatus().equals(TaskState.DELETED)) {
            throw new NoPersistExc("Restoration") ;
        }
        return true;
    }

    @Override
    public Task getTaskById(int id) {
        return taskRepo.findById(id)
                .orElseThrow(() -> new TaskNotFoundExc(id));
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    @Override
    public boolean updateTask(int task_id, Task incoming) {
        Task existing = taskRepo.findById(task_id)
            .orElseThrow(() -> new TaskNotFoundExc(task_id));
        
        existing.setDuration(incoming.getDuration());
        existing.setTimePassed(incoming.isTimePassed());
        existing.setWorkload(incoming.getWorkload());
        existing.setTaskType(incoming.getTaskType());
        existing.setStatus(incoming.getStatus());
        existing.setRequiredTAs(incoming.getRequiredTAs());
        existing.setSection(incoming.getSection());
        existing.setExam(incoming.getExam());
        
        taskRepo.save(existing);
        return taskRepo.existsById(task_id);
    }

    @Override
    public TA getTAById(int task_id, Long ta_id) {
        Task task = taskRepo.findById(task_id)
                .orElseThrow(() -> new TaskNotFoundExc(task_id));
        List<TaTask> task_list = task.getTasList() ;
        for (TaTask t : task_list){
            if (Objects.equals(t.getTaOwner().getId(), ta_id))
                return t.getTaOwner() ;
        }
        throw new GeneralExc("TA with ID " + ta_id + " not found for task with ID " + task_id);
    }

    @Override
    public boolean assignProctoring(int task_id, List<Long> ta_ids){
        Task task = taskRepo.findById(task_id)
                    .orElseThrow(() -> new TaskNotFoundExc(task_id));
        for (Long ta_id : ta_ids){
            TA ta = taRepo.findById(ta_id).orElseThrow(() -> new TaNotFoundExc(ta_id));
            if (taTaskRepo.exists(task_id, ta.getId())) {
                throw new GeneralExc("TA is already assigned to this task");
            }
            if (task.getAmountOfTas() == task.getRequiredTAs()) {
                throw new TaskLimitExc();
            }
            // Create new TaTask relationship
            TaTask taTask = new TaTask(task, ta, task.getAccessType());
            // Update task side
            task.setAmountOfTas(task.getAmountOfTas() + 1);
            if (task.getTasList() == null) {
                task.setTasList(new ArrayList<>());
            }
            // Update TA side
            if (ta.getTaTasks() == null) {
                ta.setTaTasks(new ArrayList<>());
            }
            taTaskRepo.saveAndFlush(taTask);
        }
        return true;
    }

    @Override
    public boolean assignTA(int task_id, TA ta) {
        // Find existing entities
        Task task = taskRepo.findById(task_id)
                    .orElseThrow(() -> new TaskNotFoundExc(task_id));
        // Check if assignment already exists
        if (taTaskRepo.exists(task_id, ta.getId())) {
            throw new GeneralExc("TA is already assigned to this task");
        }
        //check if ta has the task on the same duration
        for (TaTask taTask : ta.getTaTasks()) {
            if (taTask.getTask().getDuration().equals(task.getDuration())) {
                throw new GeneralExc("TA already has task on the same duration");
            }
        }
        for(Section sec : ta.getSectionsAsStudent()){
            for (Lesson lesson : sec.getLessons()){
                if(task.getDuration().has(lesson.getDuration()))
                    throw new GeneralExc("TA has lesson during that duration");
            }
        }
        // Check task limits
        if (task.getAmountOfTas() == task.getRequiredTAs()) {
            throw new TaskLimitExc();
        }
        
        TaTask taTask = new TaTask( task, ta, task.getAccessType());
        // Update task side
        task.setAmountOfTas(task.getAmountOfTas() + 1);
        if (task.getTasList() == null) {
            task.setTasList(new ArrayList<>());
        }
        // Update TA side
        if (ta.getTaTasks() == null) {
            ta.setTaTasks(new ArrayList<>());
        }
        taTaskRepo.saveAndFlush(taTask);
        return true;
    }

    @Override
    public boolean unassignTA(int task_id, TA ta) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(task_id);
        }

        Task task = (Task) taskOptional.get();
        if (task.getAmountOfTas() == 0) {
            throw new TaskNoTasExc();
        }

        //TaTaskId id = new TaTaskId(task_id,ta.getId()) ;
        Optional<TaTask> tas_taskOptional = taTaskRepo.findByTaskIdAndTaId(task_id, ta.getId());
        if (tas_taskOptional.isEmpty()) {
            throw new GeneralExc("TA not assigned to task");
        }
        TaTask taTask = tas_taskOptional.get();
        taTaskRepo.delete(taTask);
        task.getTasList().remove(taTask);
        ta.getTaTasks().remove(taTask);
        task.removeTA();
        taskRepo.saveAndFlush(task);
        taRepo.saveAndFlush(ta);
        taTaskRepo.saveAndFlush(taTask);

        tas_taskOptional = taTaskRepo.findByTaskIdAndTaId(task_id, ta.getId());
        if (tas_taskOptional.isEmpty()) {
            throw new NoPersistExc("Unassignment");
        }
        return true;
    }

    @Override
    public List<TA> getTAsByTaskId(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(task_id);
        }

        Task task = (Task) taskOptional.get();
        if (task.getAmountOfTas() == 0) {
            throw new TaskNoTasExc();
        }

        if (task.getTasList() == null) {
            throw new TaskNoTasExc();
        }
        
        List<TA> tas_list = new ArrayList<>();
        for (TaTask t : task.getTasList()) {
            if (t.getTaOwner() == null) {
                throw new GeneralExc("TA owner not found for task with ID " + task_id);
            }
            if (t.getTaOwner().getId() == null) {
                throw new GeneralExc("TA with ID " + t.getTaOwner().getId() + " not found for task with ID " + task_id);
            }
            tas_list.add(t.getTaOwner()) ;
        }
        return tas_list;
    }

    @Override
    public boolean approveTask(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(task_id);
        }
        Task task = taskOptional.get();
        approve(task);
        taskRepo.saveAndFlush(task);

        Task t = taskRepo.findById(task_id)
                .orElseThrow(() -> new TaskNotFoundExc(task_id));
        if (!t.getStatus().equals(TaskState.APPROVED)) {
            throw new NoPersistExc("Approval");
        }
    
        return true;
    }

    @Override
    public boolean rejectTask(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(task_id);
        }
        Task task = taskOptional.get();
        reject(task);
        taskRepo.saveAndFlush(task);

        Task t = taskRepo.findById(task_id)
                .orElseThrow(() -> new TaskNotFoundExc(task_id));
        if (!t.getStatus().equals(TaskState.REJECTED)) {
            throw new NoPersistExc("Rejection");
        }
    
        return true;
    }

    @Override
    public boolean checkAndUpdateStatusTask(Task task) {
        if (task == null) {
            throw new GeneralExc("Task not found!");
        }
        
        TaskState newStatus = task.isTaskActive() ? TaskState.PENDING : TaskState.NORESPOND;
        if (task.getStatus() != newStatus) {
            if (task.isTaskActive()) {
                mark_pending(task);
            } else {
                mark_not_responded(task);
                task.setTimePassed(true);
            }
            taskRepo.saveAndFlush(task);
            return true;
        }
        return false;
    }

    @Override
    public HashSet<Task> getApprovedTasks() {
        return taskRepo.findApprovedTasks();
    }

    @Override
    public List<Task> getPendingTasks() {
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
        //Undone
    }

    private void approve(Task t){
        mark_approved(t);
    }

    private void mark_rejected(Task t) {
        t.setStatus(TaskState.REJECTED);
        //Undone
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

    @Async("taskCheckExecutor")
    @Scheduled(cron = "0 * * * * *")
    public void checkTasksForTime(){
        List<Task> tasks = taskRepo.findAll();
        for(Task task : tasks){
            //log.info("status I " + task.getStatus() + " " + task.getDuration().getStart().getHour()+":"+task.getDuration().getStart().getMinute() + "/" + task.getDuration().getFinish().getHour()+":"+task.getDuration().getFinish().getMinute() + " " + "-" + Thread.currentThread().getName());
            checkAndUpdateStatusTask(task);
            //log.info("status II " + task.getStatus() + " " + task.getDuration().getStart().getHour()+":"+task.getDuration().getStart().getMinute() + "/" + task.getDuration().getFinish().getHour()+":"+task.getDuration().getFinish().getMinute() + " " + "-" + Thread.currentThread().getName());
        }
    }

}