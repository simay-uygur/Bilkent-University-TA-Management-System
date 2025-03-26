package com.example.demo1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo1.entity.TA;
import com.example.demo1.entity.Task;
import com.example.demo1.exception.UserNotFoundExc;
import com.example.demo1.service.TAServ;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TA_controller {
    @Autowired
    private final TAServ serv;

    @GetMapping("/api/ta/all")
    public List<TA> getAllTAs() 
    {
        return serv.getAllTAs();
    }

    @GetMapping("/api/ta/{id}")
    public TA getTAById(@PathVariable Long id) 
    {
        return serv.getTAById(id);
    }

    @DeleteMapping("/api/ta/{id}")
    public ResponseEntity<HttpStatus> deleteTAById(@PathVariable Long id) 
    {
        if (serv.deleteTAById(id) == false)
            throw new UserNotFoundExc(id);
        serv.deleteTAById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("/api/task/{id}")
    public Task getTaskById(@PathVariable int id) 
    {
        return serv.getTaskById(id);
    }
    @GetMapping("/api/ta/{id}/tasks")
    public List<Task> getAllTasks(Long id) 
    {
        return serv.getAllTasks(id);
    }
    // @PostMapping("/api/task")
    // public Task createTask(@RequestBody Task task) {}
    // @DeleteMapping("/api/task/{id}")
    // public ResponseEntity<?> deleteTaskById(@PathVariable int id) {}
    // @GetMapping("/api/ta/{id}/tasks")
    // public List<Task> getTasksByTAId(@PathVariable int id) {}
}
