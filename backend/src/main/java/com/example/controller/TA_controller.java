package com.example.controller;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.SectionDto;
import com.example.dto.TaDto;
import com.example.dto.TaTaskDto;
import com.example.entity.General.Date;
import com.example.entity.Schedule.ScheduleItemDto;
import com.example.entity.Tasks.TaGradingDto;
import com.example.entity.Tasks.TaProctorDto;
import com.example.entity.Tasks.Task;
import com.example.exception.GeneralExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.TaskRepo;
import com.example.service.TAServ;
import com.example.service.TaskServ;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
public class TA_controller {
    private final TAServ serv;

    private final TaskServ taskServ;

    private final TARepo taRepo;

    private final TaskRepo taskRepo;

    @GetMapping("/api/ta/all")
    public List<TaDto> getAllTAs()
    {
        return serv.getAllTAs();
    } // method should be sent to Admin controller

    @GetMapping("/api/ta/{id}")
    public TaDto getTAById(@PathVariable Long id)
    {
        return serv.getTAByIdDto(id);
    }

    @GetMapping("/api/ta/{id}/sections")
    public ResponseEntity<List<SectionDto>> getTASections(@PathVariable Long id) {
        return new ResponseEntity<>(serv.getTASections(id), HttpStatus.OK);
    }
    @GetMapping("/api/ta/{id}/task")
    public ResponseEntity<List<TaTaskDto>> getTATasks(@PathVariable Long id) {
        return new ResponseEntity<>(serv.getTATasks(id), HttpStatus.OK);
    }

    @GetMapping("/api/ta/department/{deptName}")
    public ResponseEntity<List<TaDto>> getTAByDepartment(@PathVariable String deptName)
    {
        return new ResponseEntity<>(serv.getTAsByDepartment(deptName), HttpStatus.OK);
    }

    @DeleteMapping("/api/ta/{id}")
    public ResponseEntity<HttpStatus> deleteTAById(@PathVariable Long id)
    {
        if (serv.getTAByIdDto(id) == null)
            throw new UserNotFoundExc(id);
        serv.deleteTAById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } // method should be sent to Admin controller

    @GetMapping("/api/ta/{ta_id}/task/{task_id}")
    public TaTaskDto getTaskById(@PathVariable("ta_id") Long ta_id, @PathVariable("task_id") int task_id)
    {
        return serv.getTaskById(task_id, ta_id);
    }

    @GetMapping("/api/ta/{id}/tasks")
    public List<TaTaskDto> getAllTasTasks(@PathVariable Long id)
    {
        return serv.getAllTasTasks(id);
    }

    @PostMapping("/api/ta/{id}/task/{task_id}")
    public ResponseEntity<?> createTask(@PathVariable Long id, @PathVariable int task_id)
    {
        Task task = taskRepo.findById(task_id)
                .orElseThrow(() -> new GeneralExc("Task with ID " + task_id + " not found."));
        if (task == null) {
            throw new GeneralExc("Task with ID " + task_id + " not found.");
        }
        /*if (task.getAccessType() == TaskAccessType.PRIVATE && task.getRequiredTAs() > 1) {
            throw new GeneralExc("Private tasks can only have one TA assigned.");
        }*/
        return new ResponseEntity<>(serv.assignTask(task, id),HttpStatus.CREATED);
    }

    @DeleteMapping("/api/ta/{ta_id}/task/{task_id}")
    public ResponseEntity<?> deleteTaskById(@PathVariable("ta_id") Long ta_id, @PathVariable("task_id") int task_id)
    {
        return new ResponseEntity<>(serv.deleteTaskById(task_id, ta_id),HttpStatus.OK);
    }

    @PutMapping("/api/ta/{id}")
    public ResponseEntity<?> restoreTA(@PathVariable Long id) {
        return new ResponseEntity<>(serv.restoreTAById(id), HttpStatus.OK);
    }

    @GetMapping("/api/ta/{id}/schedule")
    public ResponseEntity<List<ScheduleItemDto>> getWeeklyScheduleForTA(@PathVariable Long id) {
        Date date = new Date().currenDate() ;
        return new ResponseEntity<>(serv.getWeeklyScheduleForTA(id, date), HttpStatus.OK);
    }

    @GetMapping("/api/ta/sectionCode/{sectionCode}")
    public ResponseEntity<?> getTAsBySectionCode(@PathVariable String sectionCode) {
        return new ResponseEntity<>(serv.getTAsBySectionCode(sectionCode), HttpStatus.OK);
    }


    @GetMapping("api/ta/{taId}/assignedExams")
    public CompletableFuture<ResponseEntity<List<TaProctorDto>>> getProctoringExams(@PathVariable Long taId) {
        return serv.getAssignedExamsOfTa(taId).thenApply(proctoringList -> {
            if (proctoringList == null || proctoringList.isEmpty()) {
                // nothing found → 400 with empty body (or you could do 204 NO_CONTENT)
                return ResponseEntity
                        .badRequest()
                        .body(Collections.emptyList());
            }
            // success → 201 CREATED with the list of TaDto
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(proctoringList);
        });
    }

    @GetMapping("api/ta/{taId}/tasks/grading")
    public CompletableFuture<ResponseEntity<List<TaGradingDto>>> getGradingOfTheTa(@PathVariable Long taId) {
        return serv.getGradingsOfTheTa(taId).thenApply(gradingList -> {
            if (gradingList == null || gradingList.isEmpty()) {
                // nothing found → 400 with empty body (or you could do 204 NO_CONTENT)
                return ResponseEntity
                        .badRequest()
                        .body(Collections.emptyList());
            }
            // success → 201 CREATED with the list of TaDto
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(gradingList);
        });
    }
}
/*{
  "task_type" : "Lab",
  "duration": {
    "start": {
      "day": 28,
      "month": 3,
      "year": 2025,
      "hour": 1,
      "minute": 0
    },
    "finish": {
      "day": 28,
      "month": 3,
      "year": 2025,
      "hour": 1,
      "minute": 5
    }
  },
  "requiredTAs": 2,
  "workload": 4,
  "access_type": "PUBLIC"
}*/