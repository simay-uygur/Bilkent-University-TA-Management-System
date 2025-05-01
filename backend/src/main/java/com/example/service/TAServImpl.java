package com.example.service;

import java.util.*;

import com.example.dto.FailedRowInfo;
import com.example.entity.Actors.Role;
import com.example.entity.General.AcademicLevelType;
import com.example.repo.TaTaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.exception.UserNotFoundExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.exception.taskExc.TaskIsNotActiveExc;
import com.example.exception.taskExc.TaskNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.TaskRepo;

import java.io.IOException;
import java.io.InputStream;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TAServImpl implements TAServ {
    
    @Autowired
    private TARepo repo;

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private TaskServ taskServ;

    @Autowired
    private TaTaskRepo taTaskRepo;
    
    @Autowired
    private ScheduleServ scheduleServ;

    @Autowired
    private BCryptPasswordEncoder encoder;

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
        Optional<TaTask> optTaTask = taTaskRepo.findByTaskIdAndTaId(task_id, ta_id);
        TaTask taTask = optTaTask.orElseThrow(() -> new TaskNotFoundExc(task_id));
        return taTask.getTask();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignTask(Task task, Long id) {
        TA existingTA = repo.findById(id)
        .orElseThrow(() -> new TaNotFoundExc(id));
        
        if (!taskServ.checkAndUpdateStatusTask(task)) {
            throw new TaskIsNotActiveExc();
        }
        taskServ.assignTA(task.getTaskId(), existingTA) ;
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
        List<TaTask> TaTasks = taTaskRepo.findAllByTaId(id);
        Set<Task> tasks_list = new HashSet<>();
        for (TaTask TaTask : TaTasks) {
            Task task = TaTask.getTask();
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
        List<TaTask> TaTasks = taTaskRepo.findAllByTaId(ta.getId());
        for (TaTask TaTask : TaTasks) {
            Task task = TaTask.getTask();
            if (task != null) {
                taskServ.unassignTA(task.getTaskId(), ta);
            }
        }
        taTaskRepo.deleteAll(TaTasks);
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
        return scheduleServ.getWeeklyScheduleForTA(ta, anyCustomDate);
    }

    @Override
    public List<ScheduleItem> getScheduleOfTheDay(TA ta, String date) {
        return scheduleServ.getDaySchedule(ta, date);
    }

    //rows are
    @Override
    public Map<String, Object> importTAsFromExcel(MultipartFile file) throws IOException {
        List<TA> successfulTAs = new ArrayList<>();
        List<FailedRowInfo> failedRows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    long id = (long) row.getCell(0).getNumericCellValue();
                    String name = row.getCell(1).getStringCellValue().trim();
                    String surname = row.getCell(2).getStringCellValue().trim();
                    String webmail = row.getCell(3).getStringCellValue().trim();
                    String levelStr = row.getCell(5).getStringCellValue().trim().toUpperCase();
                    int inactiveFlag = (int) row.getCell(6).getNumericCellValue();
                    boolean isActive = (inactiveFlag == 0);
                    AcademicLevelType level = AcademicLevelType.valueOf(levelStr);

                    Optional<TA> optionalTA = repo.findByIdAndWebmail(id, webmail);

                    TA ta = optionalTA.map(existing -> {
                        existing.setName(name);
                        existing.setSurname(surname);
                        existing.setAcademicLevel(level);
                        existing.setIsActive(isActive);
                        existing.setDeleted(false); // Just in case
                        return existing;
                    }).orElseGet(() -> {
                        TA newTa = new TA();
                        newTa.setId(id);
                        newTa.setName(name);
                        newTa.setSurname(surname);
                        newTa.setWebmail(webmail);
                        newTa.setAcademicLevel(level);
                        newTa.setIsActive(isActive);
                        newTa.setRole(Role.TA);
                        newTa.setPassword(encoder.encode("default123"));
                        newTa.setTotalWorkload(0);
                        return newTa;
                    });

                    // Add to save list
                    successfulTAs.add(ta);

                } catch (Exception e) {
                    StringBuilder rawData = new StringBuilder();
                    row.forEach(cell -> rawData.append(cell.toString()).append(" | "));
                    failedRows.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }
        }

        if (!successfulTAs.isEmpty()) {
            repo.saveAll(successfulTAs);
            repo.flush();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successfulTAs.size());
        result.put("failedCount", failedRows.size());
        result.put("failedRows", failedRows);
        return result;
    }
}
