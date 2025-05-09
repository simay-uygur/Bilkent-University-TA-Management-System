package com.example.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
import com.example.exception.taskExc.TaskNotFoundExc;
import com.example.mapper.TaskMapper;
import com.example.repo.SectionRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;
import com.example.repo.TaskRepo;
import com.example.dto.TaskDto;
import com.example.entity.General.Date;
import com.example.service.RequestServices.WorkLoadServ;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class TaskServImpl implements TaskServ {

    private final TaskRepo taskRepo;
    private final TARepo taRepo;
    private final TaTaskRepo taTaskRepo;
    private final TaskMapper taskMapper;
    private final SectionRepo sectionRepo;
    private final WorkLoadServ workLoadServ;
    private final CourseOfferingServ courseOfferingServ;
    private final RequestServ reqServ;
    @Override
    public boolean assignTasToTaskByTheirId(String sectionCode, int taskId, List<Long> tas) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundExc(taskId));
        Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                .orElseThrow(() -> new GeneralExc("Section not found!"));
        if (task.getSection() != null && task.getSection().getSectionCode().equals(sectionCode)) {
            for (Long taId : tas) {
                TA ta = taRepo.findById(taId)
                        .orElseThrow(() -> new TaNotFoundExc(taId));
                assignTA(task, ta, section.getInstructor().getId());
            }
            return true;
        } else {
            throw new GeneralExc("Task not found in the specified section!");
        }
    }
    @Override
@Transactional
public boolean deleteTask(String section_code, int task_id) {
    try {
        // Get the task
        Task task = taskRepo.findById(task_id)
                .orElseThrow(() -> new TaskNotFoundExc(task_id));
        
        // Verify task belongs to the section
        if (task.getSection() == null || !task.getSection().getSectionCode().equalsIgnoreCase(section_code)) {
            throw new GeneralExc("Task not found in the specified section!");
        }
        
        // First handle the TaTask relationships using a simpler approach
        if (task.getTasList() != null && !task.getTasList().isEmpty()) {
            // Use JPQL to directly delete the relationships from the database
            // This avoids manipulating the in-memory collections which can cause issues
            taTaskRepo.deleteAllByTaskId(task.getTaskId());
            
            // Clear the collection without triggering cascades
            task.getTasList().clear();
            
            // Persist this change
            taskRepo.saveAndFlush(task);
        }
        
        // Remove the task from the section's task collection
        if (task.getSection() != null) {
            Section section = task.getSection();
            section.getTasks().remove(task);
            task.setSection(null);
            sectionRepo.saveAndFlush(section);
        }
        
        // Finally delete the task
        taskRepo.delete(task);
        return true;
    } catch (Exception e) {
        // Fallback to soft delete if hard delete fails
        log.error("Error during hard delete of task: " + e.getMessage(), e);
        return soft_deleteTask(task_id);
    }
}
   /*  public boolean deleteTask(String section_code, int task_id) {
        // Get the section
        Section section = sectionRepo.findBySectionCodeIgnoreCase(section_code)
                .orElseThrow(() -> new GeneralExc("Section not found!"));
        
        // Get the task
        Task task = taskRepo.findById(task_id)
                .orElseThrow(() -> new TaskNotFoundExc(task_id));
        
        // Verify task belongs to the section
        if (task.getSection() != null && task.getSection().getSectionCode().equals(section_code)) {
            // First, remove all TA assignments for this task
            if (task.getTasList() != null && !task.getTasList().isEmpty()) {
                // Get instructor ID for unassigning TAs
                Long instructorId = section.getInstructor().getId();
                
                // Create a new list to avoid ConcurrentModificationException
                List<TaTask> tasToRemove = new ArrayList<>(task.getTasList());
                
                // Unassign all TAs from the task
                for (TaTask taTask : tasToRemove) {
                    TA ta = taTask.getTaOwner();
                    if (ta != null) {
                        // Use existing method to properly remove the relationship
                        unassignTA(task, ta, instructorId);
                    }
                }
                
                // Make sure the list is empty
                task.getTasList().clear();
                
                // Save the task with empty TA list before deleting
                taskRepo.saveAndFlush(task);
            }
            
            // Now delete the task
            taskRepo.delete(task);
            return true;
        } else {
            throw new GeneralExc("Task not found in the specified section!");
        }
    } */
    /* public boolean deleteTask(String section_code, int task_id) {
        Section section = sectionRepo.findBySectionCodeIgnoreCase(section_code)
                .orElseThrow(() -> new GeneralExc("Section not found!"));
        Task task = taskRepo.findById(task_id)
                .orElseThrow(() -> new TaskNotFoundExc(task_id));
        if (task.getSection() != null && task.getSection().getSectionCode().equals(section_code)) {
            taskRepo.delete(task);
            return true;
        } else {
            throw new GeneralExc("Task not found in the specified section!");
        }
    } */
    @Override
    public TaskDto createTask(TaskDto taskDto, String sectionCode) {
        if (taskDto == null) {
            throw new GeneralExc("Task cannot be null!");
        }
        Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                .orElseThrow(() -> new GeneralExc("Section not found!"));
        Task task = new Task(section, taskDto.getDuration(),taskDto.getDescription(), taskDto.getType(), 0);
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
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundExc(taskId));
        unassignTas(task, instrId);
        for(TaDto taDto : tas){
            TA ta = taRepo.findById(taDto.getId()).orElseThrow(() -> new TaNotFoundExc(taDto.getId()));
            assignTA(task, ta, instrId);
        }
        return getTAsByTaskId(taskId);
    }

    @Override
    @Transactional
    public boolean assignTA(Task task, TA ta, Long instr_id) {
        // Check if assignment already exists
        if (taTaskRepo.exists(task.getTaskId(), ta.getId())) {
            throw new GeneralExc("TA is already assigned to this task");
        }
        
        if (hasDutyOrLessonOrExam(ta, task))
            throw new GeneralExc("TA has a duty or lesson or exam on the same duration as the task");
        
        task.assignTo(ta);
        taskRepo.save(task);
        reqServ.deleteAllReceivedAndSendedSwapAndTransferRequestsBySomeTime(ta, task.getDuration());
        return true;
    }


    public void unassignTas(Task task, Long instr_id){
        for(TaTask taTask : task.getTasList()){
            unassignTA(task, taTask.getTaOwner(), instr_id);
        }
    }
    @Override
    @Transactional
    public boolean unassignTA(Task task, TA ta, Long instr_id) {

        /*TaTask link = taTaskRepo.findByTaskIdAndTaId(task_id, ta.getId()).
        orElseThrow(() -> new GeneralExc("TA with id " + ta.getId() + " is not assigned to the task with id " + task.getTaskId()));
        
        task.getTasList().remove(link);   
        ta.getTaTasks().remove(link);   
        task.removeTA();                 
        taTaskRepo.delete(link);  */
        /*link.setTask(null);
        taskRepo.saveAndFlush(task); 
        link.setTaOwner(null);*/
        task.removeTA();
        taTaskRepo.deleteByTaskAndTa(task.getTaskId(), ta.getId());

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
            tas_list.add(taDto);
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

    /* @Override
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