package com.example.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.example.entity.General.DayOfWeek;
import com.example.util.TaAvailabilityChecker;
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
import com.example.entity.General.Event;
import com.example.entity.Requests.RequestType;
import com.example.entity.Requests.WorkLoadDto;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskState;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.exception.taskExc.TaskNotFoundExc;
import com.example.mapper.TaMapper;
import com.example.mapper.TaskMapper;
import com.example.repo.RequestRepos.WorkLoadRepo;
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
    private final WorkLoadRepo workLoadRepo;
    private final CourseOfferingServ courseOfferingServ;
    private final RequestServ reqServ;
    private final TaMapper taMapper;
    private final TaAvailabilityChecker availabilityChecker;
    private final LogService log;

    @Override
    public boolean unassignTasToTaskByTheirId(String sectionCode, int taskId, List<Long> taIds){
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundExc(taskId));
        Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                .orElseThrow(() -> new GeneralExc("Section not found!"));
        if (task.getSection() != null && task.getSection().getSectionCode().equals(sectionCode)) {
            for (Long taId : taIds) {
                TA ta = taRepo.findById(taId)
                        .orElseThrow(() -> new TaNotFoundExc(taId));
                unassignTA(task, ta, section.getInstructor().getId());
            }
            log.info("TAs unassignment" ,"TAs were unassigned from the task with id: " + taskId);
            return true;
        } else {
            throw new GeneralExc("Task not found in the specified section!");
        }
    }
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
            log.info("TAs assignment" ,"TAs were assigned to the task with id: " + taskId);
            return true;
        } else {
            throw new GeneralExc("Task not found in the specified section!");
        }
    }
    @Override
    @Transactional
    public boolean deleteTask(String sectionCode, int taskId) {
        // 1) load
        Section section = sectionRepo
                .findBySectionCodeIgnoreCase(sectionCode)
                .orElseThrow(() -> new GeneralExc("Section not found!"));
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundExc(taskId));

        // 2) verify ownership
        if (!Objects.equals(task.getSection(), section)) {
            throw new GeneralExc("Task not found in that section!");
        }

        // 3) break all TA links (will trigger orphanRemoval on tasList)
        task.getTasList().clear();

        // 4) also clear any other child collections you have
        task.getWorkloadList().clear();    // if you have WorkLoad children

        // 5) detach from section
        section.getTasks().remove(task);
        task.setSection(null);

        // 6) flush these changes by saving the section
        sectionRepo.save(section);

        // 7) finally delete the task
        log.info("Task deletion(hard_delete)" ,"Task with id: " + taskId + "is deleted from the system.");
        taskRepo.delete(task);
        return true;
    }
    @Override
    public TaskDto createTask(TaskDto taskDto, String sectionCode) {
        if (taskDto == null) {
            throw new GeneralExc("Task cannot be null!");
        }
        if (taskDto.getType().equals("Grading")){
            taskDto.getDuration().setStart(new Date().currenDate());
        }
        if (taskDto.getDuration().getStart().isAfter(taskDto.getDuration().getFinish()))
            throw new GeneralExc("Wrong duration! Start time can not be after the finish time.");
        Section section = sectionRepo.findBySectionCodeIgnoreCase(sectionCode)
                .orElseThrow(() -> new GeneralExc("Section not found!"));
        Task task = new Task(section, taskDto.getDuration(),taskDto.getDescription(), taskDto.getType(), 0);
        checkAndUpdateStatusTask(task);
        Task newTask = taskRepo.save(task);
        log.info("Task creation" ,"Task with id: " + newTask.getTaskId() + "is created in the system.");
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
        log.info("Task deletion(soft_delete)" ,"Task with id: " + id + "is deleted from the system.");
        return true;
    }

    @Override
    public boolean strict_deleteTask(int id) {
        taTaskRepo.deleteAllByTaskTaskId((long) id);
        taTaskRepo.flush();

        // 2) Bulk delete every workload_requests row referencing this Task
        workLoadRepo.deleteAllByTaskTaskId((long) id);
        workLoadRepo.flush();
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new TaskNotFoundExc(id));
        task.getTasList().clear();
        //   c) Detach from its Section parent
        Section parentSection = task.getSection();
        if (parentSection != null) {
            parentSection.getTasks().remove(task);
        }
        //sectionRepo.save(parentSection);
        taskRepo.saveAndFlush(task);
        log.info("Task deletion(hard_delete)" ,"Task with id: " + id + "is deleted from the system.");
        // 4) Finally delete the Task
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
        log.info("Task restoration" ,"Task with id: " + id + "is restored to the system.");
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
        log.info("Task update" ,"Task with id: " + task_id + "is updated in the system.");
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
                return t.getTaOwner();
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
        log.info("TAs assignment to the task" ,"TAs are assigned to the task with id: " + task_id);
        return true;
    }

    @Override
    @Transactional
    public List<TaDto> assignTasToTask(List<Long> tas, int taskId, int sectionNum, String courseCode, Long instrId){
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new TaskNotFoundExc(taskId));
        unassignTas(taskId, instrId);
        for(Long taId : tas){
            TA ta = taRepo.findById(taId).orElseThrow(() -> new TaNotFoundExc(taId));
            assignTA(task, ta, instrId);
        }
        taskRepo.saveAndFlush(task);
        log.info("TAs assignment to the task" ,"TAs are assigned to the task with id: " + taskId);
        return getTAsByTaskId(taskId);
    }

    @Override
    @Transactional
    public boolean assignTA(Task task, TA ta, Long instr_id) {
        // Check if assignment already exists
        if (taTaskRepo.exists(task.getTaskId(), ta.getId())) {
            throw new GeneralExc("TA is already assigned to this task");
        }

        if (hasDutyOrLessonOrExam(ta, task.getDuration()) && !task.getTaskType().toString().equals("Grading"))
            throw new GeneralExc("TA has a duty or lesson or exam on the same duration as the task");

        task.assignTo(ta);
        taskRepo.saveAndFlush(task);
        reqServ.deleteAllReceivedAndSendedSwapAndTransferRequestsBySomeTime(ta, task.getDuration());
        log.info("TA assignment to the task" ,"TA is assigned to the task with id: " + task.getTaskId());
        return true;
    }

    @Override
    @Transactional
    public boolean unassignTas(int task_id, Long instr_id) {
        Task task = taskRepo.findById(task_id)
                .orElseThrow(() -> new GeneralExc("Task with ID " + task_id + " not found."));

        // Take a copy of the current TaTask links
        List<TaTask> snapshot = new ArrayList<>( task.getTasList() );

        // Iterate the copy; each call will remove from the real tasList
        for (TaTask taTask : snapshot) {
            unassignTA(task, taTask.getTaOwner(), instr_id);
        }
        log.info("TAs unassignment from the task" ,"TAs are unassigned from the task with id: " + task.getTaskId());
        // Persist the removals
        taskRepo.saveAndFlush(task);
        return true;
    }

    @Override
    @Transactional
    public boolean unassignTA(Task task, TA ta, Long instr_id) {
        int taskId = task.getTaskId();
        TaTask link = taTaskRepo.findByTaskIdAndTaId(taskId, ta.getId())
                .orElseThrow(() -> new GeneralExc(
                        "TA with id " + ta.getId() +
                                " is not assigned to task " + taskId));

        // Remove the join from both sides
        task.getTasList().remove(link);
        ta.getTaTasks().remove(link);
        task.removeTA();           // decrement your counter
        taTaskRepo.delete(link);   // delete the row
        log.info("TA unassignment from the task" ,"TA is unassigned from the task with id: " + task.getTaskId());
        return true;
    }
    @Override
    public List<TaDto> getTAsByTaskId(int task_id) {
        Optional<Task> taskOptional = taskRepo.findById(task_id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundExc(task_id);
        }

        Task task = taskOptional.get();

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
        }
        return tas_list;
    }


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
            if (ta.isActive() && !ta.isDeleted() && !hasDutyOrLessonOrExam(ta, task.getDuration()))
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

    @Override
    public boolean hasDutyOrLessonOrExam(TA ta, Event duration) {
        LocalDate currentDate = duration.getStart().toLocalDate();
       // DayOfWeek currentDow  = DayOfWeek.valueOf(currentDate.getDayOfWeek().name());

        for (TaTask taTask : ta.getTaTasks()) {
            if (taTask.getTask().getDuration().has(duration) && !taTask.getTask().getStatus().equals(TaskState.DELETED)) {
                return true;
            }
        }
        // 3) for each lesson, first match day‐of‐week, then time‐of‐day
        boolean hasOverlappingLesson = availabilityChecker.hasOverlappingLesson(ta, duration);
        if (hasOverlappingLesson) {
            return true;
        }

        for (Exam exam : ta.getExams()){
            if (exam.getDuration().has(duration)) {
                return true;
            }
        }
        return false;
    }

    private int getDay(DayOfWeek day){
        switch(day) {
            case MONDAY:
                return 1;
            case TUESDAY:
                return 2;
            case WEDNESDAY:
                return 3;
            case THURSDAY:
                return 4;
            case FRIDAY:
                return 5;
            case SATURDAY:
                return 6;
            case SUNDAY:
                return 7;
            default:
                throw new IllegalArgumentException("Invalid day: " + day);
        }
    }

    private boolean timeOverlap(Event a, Event b) {
        int aStart = a.getStart().getHour() * 60 + a.getStart().getMinute();
        int aEnd   = a.getFinish().getHour() * 60 + a.getFinish().getMinute();
        int bStart = b.getStart().getHour() * 60 + b.getStart().getMinute();
        int bEnd   = b.getFinish().getHour() * 60 + b.getFinish().getMinute();
        return aStart < bEnd && bStart < aEnd;
    }



}
/*                if (duration.hasLesson(getDay(lesson.getDay()), lesson.getDuration())) {
                    return true;
                }
            }
        }
        for (Exam exam : ta.getExams()){
            if (exam.getDuration().has(duration)) {
                return true;
            }
        }
        return false;
    }

    private int getDay(DayOfWeek day){
        switch(day) {
            // Add cases here
            case MONDAY:
                return 1;
            case TUESDAY:
                return 2;
            case WEDNESDAY:
                return 3;
            case THURSDAY:
                return 4;
            case FRIDAY:
                return 5;
            case SATURDAY:
                return 6;
            case SUNDAY:
                return 7;
            default:
                throw new IllegalArgumentException("Invalid day: " + day);
        } */