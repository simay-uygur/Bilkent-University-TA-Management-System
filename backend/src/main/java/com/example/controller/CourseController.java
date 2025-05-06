package com.example.controller;

import java.util.List;
import java.util.stream.Collectors;

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

import com.example.dto.CourseDto;
import com.example.dto.TaDto;
import com.example.dto.TaskDto;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;
import com.example.mapper.TaMapper;
import com.example.repo.CourseRepo;
import com.example.service.CourseOfferingServ;
import com.example.service.CourseServ;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
public class CourseController {
    @Autowired
    private CourseServ courseServ; // this is used to check if the course exists in the database

    @Autowired
    private CourseRepo courseRepo;

    private final TaMapper taMapper;
    @Autowired
    private CourseOfferingServ courseOfferingServ;

    @DeleteMapping("api/course/{course_code}")
    public ResponseEntity<Boolean> deleteCourse(@PathVariable String course_code) {
        return new ResponseEntity<>(courseServ.deleteCourse(course_code), HttpStatus.OK);
    }

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
    @GetMapping("api/course/department/{deptName}")
    public ResponseEntity<List<CourseDto>> getByDepartment(
            @PathVariable String deptName ) {
        return new ResponseEntity<>(courseServ.getCoursesByDepartment(deptName), HttpStatus.FOUND);
       /*  List<CourseDto> dtos = courseServ.getCoursesByDepartment(deptName);
        return ResponseEntity.ok(dtos); */
    }

    @GetMapping("api/course/{course_code}")
    public ResponseEntity<CourseDto> getCourse(@PathVariable String course_code) {
        return new ResponseEntity<>(courseServ.findCourse(course_code), HttpStatus.FOUND);
    }

    @GetMapping("api/course/all")
    public ResponseEntity<List<CourseDto>> getCourses() {
        return new ResponseEntity<>(courseServ.getCourses(),HttpStatus.FOUND);
    }
    
    
    @PostMapping("api/course/{course_code}/ta/{ta_id}")
    public ResponseEntity<Boolean> assignTA(@PathVariable String course_code, @PathVariable Long ta_id) {
        return new ResponseEntity<>(courseOfferingServ.assignTA(ta_id, course_code), HttpStatus.OK);  // newly added
    }
//
//    @PostMapping("api/course/{course_code}/task")
//    public ResponseEntity<Boolean> createTask(@PathVariable String course_code, @RequestBody Task task) {
//        return new ResponseEntity<>(courseServ.addTask(course_code, task),HttpStatus.CREATED);
//    }

    @PutMapping("api/course/{course_code}/task/{task_id}")
    public ResponseEntity<Boolean> updateTask(@PathVariable String course_code, @RequestBody int task_id, @RequestBody Task task) {
        return new ResponseEntity<>(courseServ.updateTask(course_code,task_id,task), HttpStatus.ACCEPTED);
    }

    @GetMapping("api/course/{course_code}/task/{id}")
    public ResponseEntity<TaskDto> getTask(
            @PathVariable String course_code,
            @PathVariable int id
    ) {
        Task task = courseServ.getTaskByID(course_code, id);

        // map each TaTask → TaDto
        List<TaDto> taDtos = task.getTasList().stream()
                .map(TaTask::getTaOwner)
                .map(taMapper::toDto)
                .collect(Collectors.toList());

        TaskDto dto = new TaskDto(
                task.getTaskType().toString(),
                taDtos,
                "Task #" + task.getTaskId(),
                task.getDuration(),
                task.getStatus().toString()
        );

        return ResponseEntity.ok(dto);
    }

//    @GetMapping("api/course/{course_code}/task/{id}")
//    public TaskDto getTask(@PathVariable String course_code, @PathVariable int id) {
//        Task task = courseServ.getTaskByID(course_code, id);
//        List<TaDto> taDtos = new ArrayList<>();
//        for (TaTask taTask : task.getTasList()){
//            /*
//               private Long id;
//    private String name;
//    private String surname;
//    private String academicLevel;
//    private int totalWorkload;
//    private Boolean isActive;
//    private Boolean isGraduated;
//    private String department;
//    private List<String> courses;
//    private List<String> lessons;
//             */
//            TaDto ta = new TaDto(
//                    taTask.getTaOwner().getId(),                       // id
//                    taTask.getTaOwner().getName(),                     // name
//                    taTask.getTaOwner().getSurname(),                  // surname
//                    taTask.getTaOwner().getAcademicLevel().name(),     // academicLevel
//                    taTask.getTaOwner().getTotalWorkload(),            // totalWorkload
//                    taTask.getTaOwner().getIsActive(),                 // isActive
//                    taTask.getTaOwner().getIsGraduated(),              // isGraduated
//                    taTask.getTaOwner().getDepartment(),               // department
//                    taTask.getTaOwner().getOfferingsAsHelper().stream()          // courses → List<String>
//                            .map(CourseOffering::getCourse)
//                            .collect(Collectors.toList()),
//                    taTask.getTaOwner().getOfferingsAsStudent().stream()    // lessons → List<String>
//                            .map(Section::getSectionCode)
//                            .collect(Collectors.toList())
//            );
//
//            taDtos.add(ta);
//        }
//        String durationStr = task.getDuration() != null ? task.getDuration().toString() : null;
//
//        return new TaskDto(
//            task.getTaskType().toString(), // Convert enum to String
//            taDtos,
//            "Task #" + task.getTaskId(), // Description or customize as needed
//            durationStr,
//            task.getStatus().toString() // Convert enum to String
//        );
//    }




    /*@PostMapping("/course/{course_code}/exam")
    public ResponseEntity<Boolean> createExam(@RequestBody Exam exam, @PathVariable String course_code) {
        return new ResponseEntity<>()
    }*/
    
    
}
