package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.Coordinator_DTO;
import com.example.entity.Actors.TA;
import com.example.entity.Actors.TA_DTO;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Course_DTO;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Lesson_DTO;
import com.example.entity.Courses.Section;
import com.example.entity.Courses.Section_DTO;
import com.example.entity.General.Student;
import com.example.entity.General.Student_DTO;
import com.example.entity.Tasks.Task;
import com.example.exception.Course.CourseNotFoundExc;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.repo.CourseRepo;
import com.example.repo.SectionRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional(rollbackOn = Exception.class)
@Service
@RequiredArgsConstructor
public class CourseServImpl implements CourseServ{

    private final CourseRepo courseRepo; // this is used to check if the course exists in the database

    private final TAServ taServ;

    private final TaskServ taskServ;

    private final SectionRepo secRepo;

    @Autowired
    private TaskServ taskService;

    @Override
    public boolean addSection(String course_code, Section section){
        Optional<Course> course = courseRepo.findByCourseCode(course_code);

        if (!course.isPresent())
            throw new CourseNotFoundExc(course_code);
        Course courseObj = course.get();
        if (courseObj.getSections_list() == null)
            courseObj.setSections_list(new ArrayList<>());
        courseObj.getSections_list().add(section);
        secRepo.saveAndFlush(section);
        courseRepo.saveAndFlush(courseObj);

        return true ;
    }

    @Override
    public boolean addTask(String course_code, Task task) {
        Optional<Course> courseOpt = courseRepo.findByCourseCode(course_code);
        if(!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);
        
        Course course = courseOpt.get();
        task.setCourse(course);
        Task created = taskService.createTask(task);
        course.getTasks().add(created);
        courseRepo.save(course);
        return true;
    }

    @Override
    public boolean courseExists(String course_code) {
        return courseRepo.existsByCourseCode(course_code);
    }

    @Override
    public boolean createCourse(Course course) {
        courseRepo.saveAndFlush(course);

        return true;
    }

    @Override
    public Course_DTO findCourse(String course_code) {
        Optional<Course> courseOpt = courseRepo.findByCourseCode(course_code);

        if (!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);
        
        Course course = courseOpt.get();
        return createDTO(course);
    }

    private Course_DTO createDTO(Course course){
        Course_DTO dto = new Course_DTO();
        dto.setCourse_code(course.getCourse_code());
        dto.setDepartment(course.getCourse_dep());
        dto.setAcademical_status(course.getCourse_academic_status().toString());
        Coordinator_DTO coor_dto = new Coordinator_DTO();
        dto.setCoordinator(coor_dto);
        dto.setInstructors(null);
        dto.setPrereqs(course.getPrereq_list().split(","));
        List<Student_DTO> studDtos = new ArrayList<>();
        for (Student stud : course.getStudents_list()){
            Student_DTO stud_dto = new Student_DTO(stud.getStudent_name(), stud.getStudent_surname(), stud.getStudent_id());
            studDtos.add(stud_dto);
        }
        dto.setStudents(studDtos);
        List<Section_DTO> sections = new ArrayList<>();
        for (Section section : course.getSections_list()){
            Section_DTO sec = new Section_DTO();
            sec.setSection_code(section.getSection_code());
            List<Lesson_DTO> lessons = new ArrayList<>();
            for (Lesson lesson : section.getLessons()){
                Lesson_DTO lessonDto = new Lesson_DTO(lesson.getDuration(), lesson.getLesson_room().getClass_code());
                lessons.add(lessonDto);
            }
            sec.setDurations(lessons);
        }

        dto.setSections(sections);

        List<TA_DTO> taDtos = new ArrayList<>();
        for (TA ta : course.getCourse_tas()){
            List<String> courses = new ArrayList<>();
            for (Course c : ta.getCourses()){
                courses.add(c.getCourse_code());
            }

            List<String> lessons = new ArrayList<>();
            for(Section c : ta.getTas_own_lessons()){
                lessons.add(c.getSection_code());
            }
            TA_DTO taDto = new TA_DTO(ta.getName(), ta.getSurname(), ta.getId(), ta.getAcademic_level().toString(), 
                                      ta.getTotal_workload(), courses, lessons);
            taDtos.add(taDto);
        }

        dto.setTas(taDtos);

        return dto;
    }

    @Override
    public boolean assignTA(Long ta_id, String course_code) {
        Optional<Course> courseOpt = courseRepo.findByCourseCode(course_code);
        if (!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);

        Course course = courseOpt.get();

        TA ta = taServ.getTAById(ta_id);

        if (ta.getCourses().contains(course))
                throw new GeneralExc("TA with id " + ta_id + " is already assigned to the Course with code " + course_code);

        for (Section sec : ta.getTas_own_lessons()){
            if (sec.getCourse().getCourse_code().equals(course.getCourse_code()))
                throw new GeneralExc("TA with id " + ta_id + " can not be assigned as the TA to the Course with code " + course_code + ". TA takes this course as the lesson.");
        }

        course.getCourse_tas().add(ta);
        courseRepo.save(course);

        courseOpt = courseRepo.findByCourseCode(course_code);
        if (!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);

        Course course1 = courseOpt.get();

        if (!course1.getCourse_tas().contains(ta))
            throw new NoPersistExc("Assignment ");

        return true;
    }

    @Override
    public List<Course_DTO> getTasks(){
        List<Course> courses = courseRepo.findAll();
        List<Course_DTO> coursesDtos = new ArrayList<>();
        for (Course course : courses){
            Course_DTO courseDto = createDTO(course);
            coursesDtos.add(courseDto);
        }
        return coursesDtos;
    }

    public boolean updateTask(String course_code,int task_id,Task task)
    {
        if (taskServ.updateTask(task_id, task))
            return true;
        throw new NoPersistExc("Update ");
    }

    @Override
    public Task getTaskByID(String course_code, int task_id) {
        if (!courseRepo.existsByCourseCode(course_code))
            throw new CourseNotFoundExc(course_code);
        Optional<Task> task = courseRepo.findTask(task_id, course_code);

        if (!task.isPresent()){
            throw new GeneralExc("Task is not assigned to the course!");
        }

        return task.get();
    }
}
