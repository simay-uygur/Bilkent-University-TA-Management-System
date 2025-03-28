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

import com.example.entity.TA;
import com.example.entity.Task;
import com.example.exception.UserNotFoundExc;
import com.example.service.TAServ;
import com.example.service.TaskServ;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class TA_controller {
    @Autowired
    private TAServ serv;

    @Autowired
    private TaskServ taskServ;

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
    public Task createTask(@RequestBody Task task, @PathVariable Long id) 
    {
        return serv.assignTask(task, id);
    }
    
    @DeleteMapping("/api/ta/{ta_id}/task/{task_id}")
    public ResponseEntity<?> deleteTaskById(@PathVariable("ta_id") Long ta_id, @PathVariable("task_id") int task_id) 
    {
        serv.deleteTaskById(task_id, ta_id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("api/ta/{id}")
    public ResponseEntity<?> restoreTA(@PathVariable Long id) {
        return new ResponseEntity<>(serv.restoreTAById(id), HttpStatus.OK);
    } // method should be sent to Admin controller
}
/*{
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
  "amount_of_tas": 1,
  "workload": 4,
  "size": 1,
  "type": "Lab",
  "status": "PENDING"
} */