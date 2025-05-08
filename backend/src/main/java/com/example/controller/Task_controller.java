package com.example.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Actors.TA;
import com.example.entity.Tasks.Task;
import com.example.repo.TARepo;
import com.example.service.TaskServ;

import lombok.RequiredArgsConstructor;


@RestController
//@RequestMapping("")
@RequiredArgsConstructor
public class Task_controller {

    private final TaskServ taskServ;

    private final TARepo taRepo;
    
    /* @PostMapping("/api/task")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        return new ResponseEntity<>(taskServ.createTask(task), HttpStatus.CREATED);
    } */
    
    @PatchMapping("api/task/{id}")
    public ResponseEntity<Task> updateStatus(@PathVariable int id) {
        Task task = taskServ.getTaskById(id);
        if (task == null) {
            throw new RuntimeException("Task with ID " + id + " not found.");
        }
        taskServ.checkAndUpdateStatusTask(task);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @PutMapping("api/task/{id}/reject")
    public ResponseEntity<Boolean> rejectTask(@PathVariable int id) {
        taskServ.rejectTask(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("api/task/{id}/approve")
    public ResponseEntity<Boolean> approveTask(@PathVariable int id) {
        taskServ.approveTask(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/task/{id}")
    public ResponseEntity<Task> getTaskByID(@PathVariable int id) {
        return new ResponseEntity<>(taskServ.getTaskById(id),HttpStatus.FOUND);
    }

    @GetMapping("/api/task/all")
    public ResponseEntity<List<Task>> getAllTasks() {
        return new ResponseEntity<>(taskServ.getAllTasks(), HttpStatus.OK);
    }

    @GetMapping("/api/task/approved")
    public ResponseEntity<?> getApprovedTasks() {
        return new ResponseEntity<>(taskServ.getApprovedTasks(), HttpStatus.OK);
    }

    @GetMapping("/api/task/pending")
    public ResponseEntity<?> getPendingTasks() {
        return new ResponseEntity<>(taskServ.getPendingTasks(), HttpStatus.OK);
    }

    @GetMapping("/api/task/rejected")
    public ResponseEntity<?> getRejectedTasks() {
        return new ResponseEntity<>(taskServ.getRejectedTasks(), HttpStatus.OK);
    }   

    @GetMapping("/api/task/deleted")
    public ResponseEntity<?> getDeletedTasks() {
        return new ResponseEntity<>(taskServ.getDeletedTasks(), HttpStatus.OK);
    }

    @PutMapping("/api/task/{task_id}/assign/{ta_id}")
    public ResponseEntity<Boolean> assignTA(@PathVariable int task_id, @PathVariable Long ta_id) {
        TA ta = taRepo.findById(ta_id)
                .orElseThrow(() -> new RuntimeException("TA with ID " + ta_id + " not found."));
        return new ResponseEntity<>(taskServ.assignTA(task_id, ta),HttpStatus.OK);
    }   

    @PutMapping("/api/task/{task_id}/unassign/{ta_id}")
    public ResponseEntity<Boolean> unassignTA(@PathVariable int task_id, @PathVariable Long ta_id) {
        TA ta = taRepo.findById(ta_id)
                .orElseThrow(() -> new RuntimeException("TA with ID " + ta_id + " not found."));
        return new ResponseEntity<>(taskServ.unassignTA(task_id, ta),HttpStatus.OK);
    }

    @GetMapping("/api/task/{task_id}/tas")
    public ResponseEntity<?> getTAsByTaskId(@PathVariable int task_id) {
        return new ResponseEntity<>(taskServ.getTAsByTaskId(task_id), HttpStatus.OK);
    }

    @GetMapping("/api/task/{task_id}/tas/{ta_id}")
    public ResponseEntity<?> getTAByTaskId(@PathVariable int task_id, @PathVariable Long ta_id) {
        return new ResponseEntity<>(taskServ.getTAById(task_id, ta_id), HttpStatus.OK);
    }

    @DeleteMapping("/api/task/{task_id}/softdelete")
    public ResponseEntity<?> softDeleteTaskById(@PathVariable int task_id) {
        return new ResponseEntity<>(taskServ.soft_deleteTask(task_id), HttpStatus.OK);
    }

    @DeleteMapping("/api/task/{task_id}/harddelete")
    public ResponseEntity<?> hardDeleteTaskById(@PathVariable int task_id) {
        return new ResponseEntity<>(taskServ.strict_deleteTask(task_id), HttpStatus.OK);
    }

    @PutMapping("/api/task/{task_id}/restore")
    public ResponseEntity<?> restoreTaskById(@PathVariable int task_id) {
        return new ResponseEntity<>(taskServ.restoreTask(task_id), HttpStatus.OK);
    }

    
}
