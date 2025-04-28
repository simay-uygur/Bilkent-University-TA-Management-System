package com.example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Actors.TA_DTO;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.CourseCodeConverter;
import com.example.entity.Courses.Course_DTO;
import com.example.entity.Courses.Section;
import com.example.entity.Exams.Exam;
import com.example.entity.Tasks.TA_Task;
import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.Task_DTO;
import com.example.exception.Course.NoPrereqCourseFound;
import com.example.repo.CourseRepo;
import com.example.service.CourseServ;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
public class Course_controller {
    @Autowired
    private CourseServ courseServ; // this is used to check if the course exists in the database

    @Autowired
    private CourseRepo courseRepo;

    @PostMapping("api/course")
    public ResponseEntity<Boolean> createCourse(@RequestBody Course course) {
        // checkPrerequisities(course); //still not done
        return new ResponseEntity<>(courseServ.createCourse(course),HttpStatus.CREATED);
        /*  
            // input json body to create course
            {
                "course_code" : "cs-299",
                "course_name" : "Summer Training I",
                "course_academic_status" : "MS",
                "course_dep"  : "IE",
                "prereq_list" : "cs-201,math-102"
            }
        */
    }

    @PostMapping("api/course/{course_code}/section")
    public ResponseEntity<?> createSection(@PathVariable String course_code, @RequestBody Section section) {
        return new ResponseEntity<>(courseServ.addSection(course_code, section), HttpStatus.CREATED);
        /*
            {
                "section_code" : "cs319-1"
            }
        */
    }

    @GetMapping("api/course/{course_code}")
    public ResponseEntity<Course_DTO> getCourse(@PathVariable String course_code) {
        return new ResponseEntity<>(courseServ.findCourse(course_code), HttpStatus.FOUND);
    }

    @GetMapping("api/course/all")
    public ResponseEntity<List<Course_DTO>> getCourses() {
        return new ResponseEntity<>(courseServ.getCourses(),HttpStatus.FOUND);
    }
    
    
    @PostMapping("api/course/{course_code}/ta/{ta_id}")
    public ResponseEntity<Boolean> assignTA(@PathVariable String course_code, @PathVariable Long ta_id) {
        return new ResponseEntity<>(courseServ.assignTA(ta_id, course_code), HttpStatus.OK);
    }

    @PostMapping("api/course/{course_code}/task")
    public ResponseEntity<Boolean> createTask(@PathVariable String course_code, @RequestBody Task task) {
        return new ResponseEntity<>(courseServ.addTask(course_code, task),HttpStatus.CREATED);
    }

    @PutMapping("api/course/{course_code}/task/{task_id}")
    public ResponseEntity<Boolean> updateTask(@PathVariable String course_code, @RequestBody int task_id, @RequestBody Task task) {
        return new ResponseEntity<>(courseServ.updateTask(course_code,task_id,task), HttpStatus.ACCEPTED);
    }

    @GetMapping("api/course/{course_code}/task/{id}")
    public Task_DTO getTask(@PathVariable String course_code, @PathVariable int id) {
        Task task = courseServ.getTaskByID(course_code, id);
        List<TA_DTO> taDtos = new ArrayList<>();
        for (TA_Task taTask : task.getTas_list()){
            TA_DTO ta = new TA_DTO(taTask.getTa_owner().getName(), taTask.getTa_owner().getSurname(), taTask.getTa_owner().getId(), null, 0, null, null);
            taDtos.add(ta);
        }
        String durationStr = task.getDuration() != null ? task.getDuration().toString() : null;
        
        return new Task_DTO(
            task.getTask_type().toString(), // Convert enum to String
            taDtos,
            "Task #" + task.getTask_id(), // Description or customize as needed
            durationStr,
            task.getStatus().toString() // Convert enum to String
        );
    }


    private void checkPrerequisites(Course course) {
        if (course.getPrereq_list() != null) {
            String[] prereqs = course.getPrereq_list().split(",");
            for (String course_code : prereqs){
                int id = new CourseCodeConverter().code_to_id(course_code);
                if (!courseRepo.existsById(id)){
                    throw new NoPrereqCourseFound(course_code);
                }
            }
        }
    }



    /*@PostMapping("/course/{course_code}/exam")
    public ResponseEntity<Boolean> createExam(@RequestBody Exam exam, @PathVariable String course_code) {
        return new ResponseEntity<>()
    }*/
    

}
