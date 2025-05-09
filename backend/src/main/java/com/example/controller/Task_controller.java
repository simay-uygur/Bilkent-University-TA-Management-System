package com.example.controller;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.TaDto;
import com.example.dto.TaskDto;
import com.example.entity.Actors.TA;
import com.example.entity.Tasks.Task;
import com.example.exception.GeneralExc;
import com.example.repo.TARepo;
import com.example.repo.TaskRepo;
import com.example.service.TaskServ;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;




@RestController
//@RequestMapping("")
@RequiredArgsConstructor
public class Task_controller {

    private final TaskServ taskServ;

    private final TARepo taRepo;

    private final TaskRepo taskRepo;
    
    /*@PostMapping("/api/task")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        return new ResponseEntity<>(taskServ.createTask(task), HttpStatus.CREATED);
    }*/
    
    
    @PatchMapping("api/task/{id}")
    public ResponseEntity<Task> updateStatus(@PathVariable Integer id) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new GeneralExc("Task with ID " + id + " not found."));
        if (task == null) {
            throw new RuntimeException("Task with ID " + id + " not found.");
        }
        taskServ.checkAndUpdateStatusTask(task);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @PutMapping("path/{id}")
    public String putMethodName(@PathVariable String id, @RequestBody String entity) {
        //TODO: process PUT request
        
        return entity;
    }

    /*@PutMapping("api/task/{id}/reject")
    public ResponseEntity<Boolean> rejectTask(@PathVariable int id) {
        taskServ.rejectTask(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("api/task/{id}/approve")
    public ResponseEntity<Boolean> approveTask(@PathVariable int id) {
        taskServ.approveTask(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }*/

    @GetMapping("/api/task/{id}")
    public ResponseEntity<TaskDto> getTaskByID(@PathVariable int id) {
        return new ResponseEntity<>(taskServ.getTaskById(id),HttpStatus.FOUND);
    }

    @GetMapping("/api/task/all")
    public ResponseEntity<List<Task>> getAllTasks() {
        return new ResponseEntity<>(taskServ.getAllTasks(), HttpStatus.OK);
    }

    /*@GetMapping("/api/task/approved")
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
    }   */

    @GetMapping("/api/task/deleted")
    public ResponseEntity<?> getDeletedTasks() {
        return new ResponseEntity<>(taskServ.getDeletedTasks(), HttpStatus.OK);
    }

    

    @PutMapping("/api/course/{course_code}/section/{section_num}/instr/{instr_id}/task/{task_id}/assign")
    @Transactional
    public ResponseEntity<List<TaDto>> assignTA(@PathVariable int task_id, @RequestBody List<Long> tas, @PathVariable Long instr_id, @PathVariable int section_num, @PathVariable String course_code) {
        return new ResponseEntity<>(taskServ.assignTasToTask(tas, task_id, section_num, course_code, instr_id),HttpStatus.OK);
    } 
    @PutMapping("/api/task/sectionCode/{sectionCode}/task/{taskId}/assign")
    @Transactional
    public ResponseEntity<Boolean> assignTAs(@PathVariable String sectionCode, @PathVariable int taskId, @RequestBody List<Long> taIds) {
        return new ResponseEntity<>(taskServ.assignTasToTaskByTheirId(sectionCode, taskId, taIds),HttpStatus.OK);
    } 
}


    /* @PutMapping("/api/sectionCode/{sectionCode}/task/{taskId}/assign/{taId}")
    @Transactional
    public ResponseEntity<Boolean> assignTAtoTask(@PathVariable String sectionCode,@PathVariable int taskId,@PathVariable Long taId) {
        return new ResponseEntity<>(taskServ.assignTaToTask(sectionCode, taskId, taId),HttpStatus.OK);
    }  -----------------Dont Use its very bad design---------------- */


  /*   @PutMapping("/api/sectionCode/{sectionCode}/instr/{instrId}/task/{taskId}/assign/{ta_id}")
    @Transactional
    public ResponseEntity<List<TaDto>> assignTAtoTask(@PathVariable int task_id, @RequestBody List<TaDto> tas, @PathVariable Long instr_id, @PathVariable int section_num, @PathVariable String course_code) {
        return new ResponseEntity<>(taskServ.assignTasToTask(tas, task_id, sectionCode instr_id),HttpStatus.OK);
    } 
 */
    /*@PutMapping("/api/instr/{instr_id}/task/{task_id}/unassign/{ta_id}")
    public ResponseEntity<Boolean> unassignTA(@PathVariable int task_id, @PathVariable Long ta_id, @PathVariable Long instr_id) {
        TA ta = taRepo.findById(ta_id)
                .orElseThrow(() -> new GeneralExc("TA with ID " + ta_id + " not found."));
        Task task = taskRepo.findById(task_id).orElseThrow(() -> new GeneralExc("Task with ID " + task_id + " not found."));
        return new ResponseEntity<>(taskServ.unassignTA(task, ta, instr_id),HttpStatus.OK);
    }

    @PutMapping("/api/instr/{instr_id}/task/{task_id}/unassign/tas")
    public ResponseEntity<Boolean> unassignTA(@PathVariable int task_id, @PathVariable Long instr_id) {
        return new ResponseEntity<>(taskServ.unassignTas(task_id, instr_id),HttpStatus.OK);
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

    @GetMapping("api/course/{course_code}/section/{section_code}/task/{task_id}/assign")
    public CompletableFuture<ResponseEntity<List<TaDto>>> findAvailableTasToAssignToTask(@PathVariable String course_code, @PathVariable String section_code, @PathVariable int task_id, @PathVariable Long instr_id) {
        return taskServ.getTasToAssignToTask(course_code, section_code, task_id, instr_id).thenApply(taList -> {
            if (taList == null || taList.isEmpty()) {
                // nothing found → 400 with empty body (or you could do 204 NO_CONTENT)
                return ResponseEntity
                    .badRequest()
                    .body(Collections.emptyList());
            }
            // success → 201 CREATED with the list of TaDto
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(taList);
        });
    }
}
*/