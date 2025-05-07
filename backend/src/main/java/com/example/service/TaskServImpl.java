package com.example.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.dto.TaDto;
import com.example.dto.TaskDto;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Date;
import com.example.entity.Requests.RequestType;
import com.example.entity.Requests.WorkLoadDto;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskState;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.exception.taskExc.TaskNoTasExc;
import com.example.exception.taskExc.TaskNotFoundExc;
import com.example.mapper.TaskMapper;
import com.example.repo.SectionRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;
import com.example.repo.TaskRepo;
import com.example.service.RequestServices.WorkLoadServ;

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

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private SectionRepo sectionRepo;

    @Autowired
    private SectionServ sectionServ;

    @Autowired 
    private WorkLoadServ workLoadServ;

    @Autowired
    private CourseOfferingServ courseOfferingServ;

    @Override
    public TaskDto createTask(TaskDto taskDto, String sectionCode) {
        if (taskDto == null) {
            throw new GeneralExc("Task cannot be null!");
        }
        Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                .orElseThrow(() -> new GeneralExc("Section not found!"));
        Task task = new Task(section, taskDto.getDuration(), taskDto.getType(), 0);
        checkAndUpdateStatusTask(task);
        Task newTask = taskRepo.save(task);
        return taskMapper.toDto(newTask);
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
        taskRepo.deleteById(id);
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
    public TaskDto getTaskById(int id) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new TaskNotFoundExc(id));
        return taskMapper.toDto(task);
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
        existing.setWorkload(incoming.getWorkload());
        existing.setTaskType(incoming.getTaskType());
        existing.setStatus(incoming.getStatus());
        existing.setSection(incoming.getSection());
        
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
            // Create new TaTask relationship
            TaTask taTask = new TaTask(task, ta);
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
    public List<TaDto> assignTasToTask(List<TaDto> tas, int taskId, int sectionNum, String courseCode, Long instrId){
        Section section = courseOfferingServ.getSectionByNumber(courseCode, sectionNum);
        for(TaDto taDto : tas){
            TA ta = taRepo.findById(taDto.getId()).orElseThrow(() -> new TaNotFoundExc(taDto.getId()));
            assignTA(taskId, ta, instrId);
        }
        return getTAsByTaskId(taskId);
    }

    @Override
    @Transactional
    public boolean assignTA(int task_id, TA ta, Long instr_id) {
        // Find existing entities
        Task task = taskRepo.findById(task_id)
                    .orElseThrow(() -> new TaskNotFoundExc(task_id));
        // Check if assignment already exists
        if (taTaskRepo.exists(task_id, ta.getId())) {
            throw new GeneralExc("TA is already assigned to this task");
        }
        //check if ta has the task on the same duration
        if (hasDutyOrLessonOrExam(ta, task))
            throw new GeneralExc("TA has a duty or lesson or exam on the same duration as the task");
        
        task.assignTo(ta);
        taskRepo.save(task);
        return true;
    }

    @Override
    public boolean unassignTA(int task_id, TA ta, Long instr_id) {
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
    public List<TaDto> getTAsByTaskId(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(task_id);
        }

        Task task = (Task) taskOptional.get();
        
        List<TaDto> tas_list = new ArrayList<>();
        for (TaTask t : task.getTasList()) {
            if (t.getTaOwner() == null) {
                throw new GeneralExc("TA owner not found for task with ID " + task_id);
            }
            if (t.getTaOwner().getId() == null) {
                throw new GeneralExc("TA with ID " + t.getTaOwner().getId() + " not found for task with ID " + task_id);
            }
            TA ta = t.getTaOwner();
            TaDto taDto = new TaDto() ;
            taDto.setId(ta.getId());
            taDto.setName(ta.getName());
            taDto.setSurname(ta.getSurname());
            //taDto.se
        }
        return tas_list;
    }

    /*@Override
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
    }*/

    /*@Override
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
    }*/

    /*@Override
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
    }*/

    public boolean checkAndUpdateStatusTask(Task task) {
        if (task == null) {
            throw new GeneralExc("Task not found!");
        }
        
        if (task.getStatus() == TaskState.DELETED) {
            return false;
        }
        Date current = new Date().currenDate();
        if (current.isBefore(task.getDuration().getStart()))
            {mark_not_active(task);}
        else if (current.isAfter(task.getDuration().getStart()) && current.isBefore(task.getDuration().getFinish()))
            {mark_active(task);}
        else{
            mark_completed(task); 
            for (TaTask ta : task.getTasList()){
                TA t = ta.getTaOwner(); 
                WorkLoadDto workLoadDto = new WorkLoadDto();
                workLoadDto.setTaskId(task.getTaskId());
                workLoadDto.setWorkload(task.getWorkload());
                workLoadDto.setTaskType(task.getTaskType().toString());
                workLoadDto.setSenderId(t.getId());
                workLoadDto.setSenderName(t.getName() + " " + t.getSurname());
                workLoadDto.setRequestType(RequestType.WorkLoad);
                workLoadDto.setSentTime(new Date().currenDate());
                workLoadDto.setReceiverId(task.getSection().getInstructor().getId());
                workLoadDto.setReceiverName(task.getSection().getInstructor().getName() + " " + task.getSection().getInstructor().getSurname());
                workLoadDto.setDescription("Workload request from " + 
                                            workLoadDto.getSenderName() + " for task " + 
                                            task.getTaskId() + " of type " + 
                                            task.getTaskType().toString() + " with workload of " + 
                                            task.getWorkload() + " hours.");
                workLoadServ.createWorkLoad(workLoadDto, t.getId());
            }
        }
        taskRepo.saveAndFlush(task);
        return true;
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

    /*private void mark_approved(Task t) {
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
    }*/

    private void mark_not_active(Task t) {
        t.setStatus(TaskState.NOT_ACTIVE);
    }

    private void mark_active(Task t) {
        t.setStatus(TaskState.ACTIVE);
    }

    private void mark_completed(Task t) {
        t.setStatus(TaskState.COMPLETED);
    }

    @Async("taskCheckExecutor")
    @Scheduled(cron = "0 * * * * *")
    public void checkTasksForTime(){
        List<Task> tasks = taskRepo.findByStatusNotIn(
            List.of(TaskState.COMPLETED, TaskState.DELETED)
        );
        for(Task task : tasks){
            //log.info("status I " + task.getStatus() + " " + task.getDuration().getStart().getHour()+":"+task.getDuration().getStart().getMinute() + "/" + task.getDuration().getFinish().getHour()+":"+task.getDuration().getFinish().getMinute() + " " + "-" + Thread.currentThread().getName());
            checkAndUpdateStatusTask(task);
            //log.info("status II " + task.getStatus() + " " + task.getDuration().getStart().getHour()+":"+task.getDuration().getStart().getMinute() + "/" + task.getDuration().getFinish().getHour()+":"+task.getDuration().getFinish().getMinute() + " " + "-" + Thread.currentThread().getName());
        }
    }

    @Async("setExecutor")
    public CompletableFuture<List<TaDto>> getTasToAssignToTask(String courseCode, String sectionCode, int task_id, Long instrId){
        Task task = taskRepo.findById(task_id)
                .orElseThrow(() -> new TaskNotFoundExc(task_id));
        List<TaDto> tas = new ArrayList<>();
        
        Section section = courseOfferingServ.getSectionByNumber(courseCode, Integer.parseInt(sectionCode));
        for (TA ta : section.getAssignedTas()){
            if (ta.isActive() && !ta.isDeleted() && !hasDutyOrLessonOrExam(ta, task))
            {
                TaDto taDto = new TaDto();
                taDto.setId(ta.getId());
                taDto.setName(ta.getName());
                taDto.setSurname(ta.getSurname());
                tas.add(taDto);
            }
        }
        return CompletableFuture.completedFuture(tas);
    }

    public boolean hasDutyOrLessonOrExam(TA ta, Task task) {
        for (TaTask taTask : ta.getTaTasks()) {
            if (taTask.getTask().getDuration().has(task.getDuration())) {
                return true;
            }
        }
        for (Section section : ta.getSectionsAsStudent()){
            for (Lesson lesson : section.getLessons()) {
                if (lesson.getDuration().has(task.getDuration())) {
                    return true;
                }
            }
        }
        for (Exam exam : ta.getExams()){
            if (exam.getDuration().has(task.getDuration())) {
                return true;
            }
        }
        return false;
    }
}