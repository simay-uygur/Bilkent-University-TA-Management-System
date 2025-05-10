package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.ExamDto;
import com.example.dto.FailedRowInfo;
import com.example.dto.SectionDto;
import com.example.dto.TaDto;
import com.example.dto.TaTaskDto;
import com.example.dto.TaskDto;
import com.example.entity.Actors.Role;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Section;
import com.example.entity.Exams.Exam;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.Date;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;
import com.example.entity.Schedule.ScheduleItemDto;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.exception.UserNotFoundExc;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.exception.taskExc.TaskIsNotActiveExc;
import com.example.exception.taskExc.TaskNotFoundExc;
import com.example.mapper.ExamMapper;
import com.example.mapper.SectionMapper;
import com.example.mapper.TaMapper;
import com.example.repo.SectionRepo;
import com.example.mapper.TaTaskMapper;
import com.example.repo.ExamRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TAServImpl implements TAServ {
    
    private final TARepo repo;
    private final TaMapper taMapper;
    private final TaskServ taskServ;
    private final TaTaskRepo taTaskRepo;
    private final ScheduleServ scheduleServ;
    private final PasswordEncoder encoder;
    private final TaTaskMapper taTaskMapper;
    private final ExamRepo examRepo;
    private final ExamMapper examMapper;
private final SectionMapper sectionMapper;
    private final SectionRepo sectionRepo;
   

    @Override
public List<TaTaskDto> getTATasks(Long id) {
    TA ta = getTAByIdEntity(id);
    if (ta == null) {
        throw new UserNotFoundExc(id);
    }
    ta.getTaTasks();
    List<TaTaskDto> taskDtos = new ArrayList<>();
    for (TaTask taTask : ta.getTaTasks()) {
        TaTaskDto taskDto = taTaskMapper.toDto(taTask);
        taskDtos.add(taskDto);
    }
    return taskDtos;
}
    @Override
    public List<SectionDto> getTASections(Long id) {
         TA ta = getTAByIdEntity(id);
    if (ta == null) {
        throw new UserNotFoundExc(id);
}
    ta.getSectionsAsHelper();
    List<SectionDto> sectionDtos = new ArrayList<>();
    for (Section section : ta.getSectionsAsHelper()) {
        SectionDto sectionDto = sectionMapper.toDto(section);
        sectionDtos.add(sectionDto);
    }
    return sectionDtos;
}
    @Override
    public List<TaDto> getTAsBySectionCode(String sectionCode){
        List<TA> tas = sectionRepo.findTasBySectionCode(sectionCode);
        if (tas.isEmpty()) {
            throw new UserNotFoundExc(sectionCode);
        }
        return tas.stream()
                .map(taMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public TaDto getTAByIdDto(Long id){
        TA ta = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundExc(id));
        return taMapper.toDto(ta);
    }

    @Override
    public TA getTAByIdTa(Long id){
        TA ta = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundExc(id));
        return ta;

    }

    @Override
    public TA getTAByIdEntity(Long id){
        return repo.findById(id)
                .orElseThrow(() -> new UserNotFoundExc(id));
        
    }

    @Override
        public List<TaDto> getTAsByDepartment(String deptName){
            List<TA> tas = repo.findByDepartment(deptName);
            if (tas.isEmpty()) {
                throw new UserNotFoundExc(deptName);
            }
            return tas.stream()
                    .map(taMapper::toDto)
                    .collect(Collectors.toList());
    } 

    @Override
    public List<TaDto> getAllTAs() {
        List<TA> tas = repo.findAll();
        if (tas.isEmpty()) {
            throw new UserNotFoundExc("TAs");
        }   
        return tas.stream()
                .map(taMapper::toDto)
                .collect(Collectors.toList());
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
    public TaTaskDto getTaskById(int task_id, Long ta_id) {
        Optional<TaTask> optTaTask = taTaskRepo.findByTaskIdAndTaId(task_id, ta_id);
        TaTask taTask = optTaTask.orElseThrow(() -> new TaskNotFoundExc(task_id));
        return taTaskMapper.toDto(taTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignTask(Task task, Long id) {
        TA existingTA = repo.findById(id)
        .orElseThrow(() -> new TaNotFoundExc(id));
        
        if (!taskServ.checkAndUpdateStatusTask(task)) {
            throw new TaskIsNotActiveExc();
        }
        //taskServ.assignTA(task.getTaskId(), existingTA) ;
        return true ;
    }
    
    @Override
    public boolean deleteTaskById(int task_id, Long ta_id) {
        Optional<TA> taOptional = repo.findById(ta_id);
        if (taOptional.isEmpty()) {
            throw new TaNotFoundExc(ta_id);
        }
        TA ta = taOptional.get();
        //taskServ.unassignTA(task_id, ta) ; 
        return true ;
    }

    @Override
    public List<TaTaskDto> getAllTasTasks(Long id) {
        TA existingTA = repo.findById(id)
            .orElseThrow(() -> new TaNotFoundExc(id));
        List<TaTaskDto> tasks = new ArrayList<>();
        for (TaTask ta_task : existingTA.getTaTasks()) {
            tasks.add(taTaskMapper.toDto(ta_task));
        }
        return tasks;
    }

    private void markDeleted(TA ta) {
        if (ta.isDeleted()) {
            throw new GeneralExc("User already deleted");
        }
        ta.setDeleted(true);
        List<TaTask> ta_tasks = taTaskRepo.findAllByTaId(ta.getId());
        for (TaTask ta_task : ta_tasks) {
            Task task = ta_task.getTask();
            if (task != null) {
                //taskServ.unassignTA(task.getTaskId(), ta);
            }
        }
        taTaskRepo.deleteAll(ta_tasks);
        taTaskRepo.flush();
        repo.saveAndFlush(ta);
    }

    private void delete(TA ta){
        markDeleted(ta);
    }

    private void restore(TA ta) {
        if (!ta.isDeleted()) {
            throw new GeneralExc("User is not deleted");
        }
        ta.setDeleted(false);
    }

    @Override
    public List<ScheduleItemDto> getWeeklyScheduleForTA(Long id, Date anyCustomDate) {
        return scheduleServ.getWeeklySchedule(id, anyCustomDate.toLocalDateTime());
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

                    TA ta = optionalTA.map(existing -> { existing.setName(name);
                        existing.setSurname(surname);
                        existing.setAcademicLevel(level);
                        existing.setActive(isActive);
                        existing.setDeleted(false); // Just in case
                        return existing;
                    }).orElseGet(() -> {
                        TA newTa = new TA();
                        newTa.setId(id);
                        newTa.setName(name);
                        newTa.setSurname(surname);
                        newTa.setWebmail(webmail);
                        newTa.setAcademicLevel(level);
                        newTa.setActive(isActive);
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

  /*   private List<TaDto> mapToDtoList(List<TA> tas) {
        List<TaDto> taDtos = new ArrayList<>();
        for (TA ta : tas) {
            TaDto taDto = new TaDto(
                    ta.getId(),
                    ta.getName(),
                    ta.getSurname(),
                    ta.getAcademicLevel().name(),
                    ta.getTotalWorkload(),
                    ta.getIsActive(),
                    ta.getIsGraduated(),
                    ta.getDepartment(),
                    ta.getCourses().stream()
                            .map(Course::getCourseCode)
                            .collect(Collectors.toList()),
                    ta.getTasOwnLessons().stream()
                            .map(Section::getSectionCode)
                            .collect(Collectors.toList())
            );
            taDtos.add(taDto);
        }
        return taDtos;
    } */

    @Override
    public List<ExamDto> getAssignedExamsOfTa(Long taId){
        return examRepo.findAllByTaId(taId)
                       .stream()
                       .filter(Exam::getIsActive)   // optional – drop if you want inactive too
                       .map(examMapper::toDto)
                       .toList();
    }
}