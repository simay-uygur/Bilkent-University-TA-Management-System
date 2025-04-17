package com.example.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskAccessType;
import com.example.exception.UserNotFoundExc;
import com.example.service.TAServ;
import com.example.service.TaskServ;
import com.example.service.UserServ;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
public class TA_controller {
    @Autowired
    private TAServ serv;

    private UserServ userServ;

    @Autowired
    private TaskServ taskServ;

    /*@PostMapping("/api/ta/signUp")
    public ResponseEntity<TA> createTA(@RequestBody TA ta) 
    {
        //System.out.println("role: " + u.getRole() + "id: " + u.getId());
        TA ta_to_check = serv.getTAById(ta.getId());
        if (ta_to_check != null)
            throw new UserExistsExc(ta.getId()) ;
        String check_mail = ta.getName().toLowerCase() + 
                            "." + 
                            ta.getSurname().toLowerCase() + 
                            "@ug.bilkent.edu.tr";
        if (!check_mail.matches(ta_to_check.getWebmail().toLowerCase()) && !Objects.equals(ta_to_check.getId(), ta_to_check.getId()))
            throw new IncorrectWebMailException() ;
        return new ResponseEntity<>((TA) userServ.createUser(ta), HttpStatus.CREATED) ;
        //return ResponseEntity.created(URI.create("/signIn/{id}")).body(serv.createUser(u)) ;
    } // method should be sent to Admin controller*/

    @GetMapping("/api/ta/all")
    public List<TA> getAllTAs() 
    {
        return serv.getAllTAs();
    } // method should be sent to Admin controller

    @GetMapping("/api/ta/{id}")
    public TA getTAById(@PathVariable Long id) 
    {
        return serv.getTAById(id);
    }

    @DeleteMapping("/api/ta/{id}")
    public ResponseEntity<HttpStatus> deleteTAById(@PathVariable Long id) 
    {
        if (serv.getTAById(id) == null)
            throw new UserNotFoundExc(id);
        serv.deleteTAById(id);
        TA a = serv.getTAById(id);
        System.out.println("ta2: " + a.isDeleted());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } // method should be sent to Admin controller

    @GetMapping("/api/ta/{ta_id}/task/{task_id}")
    public Task getTaskById(@PathVariable("ta_id") Long ta_id, @PathVariable("task_id") int task_id) 
    {
        return serv.getTaskById(task_id, ta_id);
    }

    @GetMapping("/api/ta/{id}/tasks")
    public Set<Task> getAllTasks(@PathVariable Long id) 
    {
        return serv.getAllTasks(id);
    }
    
    @PostMapping("/api/ta/{id}/task")
    public Task createTask(@RequestBody Task task, @PathVariable Long id, @PathVariable String type) 
    {
        TaskAccessType taskType = TaskAccessType.valueOf(type.toUpperCase());
        if (taskType == null) {
            throw new IllegalArgumentException("Invalid task type: " + type);
        }
        serv.assignTask(task, id, taskType);
        return serv.getTaskById(task.getTask_id(), id);
    }
    
    @DeleteMapping("/api/ta/{ta_id}/task/{task_id}")
    public ResponseEntity<?> deleteTaskById(@PathVariable("ta_id") Long ta_id, @PathVariable("task_id") int task_id) 
    {
        return new ResponseEntity<>(serv.deleteTaskById(task_id, ta_id),HttpStatus.OK);
    }

    @PutMapping("api/ta/{id}")
    public ResponseEntity<?> restoreTA(@PathVariable Long id) {
        return new ResponseEntity<>(serv.restoreTAById(id), HttpStatus.OK);
    } 

    @GetMapping("/api/ta/{id}/schedule")
    public ResponseEntity<?> getWeeklyScheduleForTA(@PathVariable Long id) {
        TA ta = serv.getTAById(id);
        Date date = new Date().currenDate() ;
        return new ResponseEntity<>(serv.getWeeklyScheduleForTA(ta, date), HttpStatus.OK);
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
  "type": "PUBLIC"
}*/