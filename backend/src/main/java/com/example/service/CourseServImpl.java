package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.ExcelHelpers.FailedRowInfo;
import com.example.entity.Courses.*;
import com.example.entity.General.AcademicLevelType;
import com.example.exception.Course.NoPrereqCourseFound;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Actors.Instructor_DTO;
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
import com.example.repo.DepartmentRepo;
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

    private final DepartmentRepo departmentRepo;

    @Override
    public boolean addSection(String course_code, Section section){
        Optional<Course> course = courseRepo.findCourseByCourseCode(course_code);

        if (!course.isPresent())
            throw new CourseNotFoundExc(course_code);
        Course courseObj = course.get();
        if (courseObj.getSectionsList() == null)
            courseObj.setSectionsList(new ArrayList<>());
        courseObj.getSectionsList().add(section);
        secRepo.saveAndFlush(section);
        courseRepo.saveAndFlush(courseObj);

        return true ;
    }

    @Override
    public boolean addTask(String course_code, Task task) {
        Optional<Course> courseOpt = courseRepo.findCourseByCourseCode(course_code);
        if(!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);

        Course course = courseOpt.get();
        task.setCourse(course);
        Task created = taskServ.createTask(task);
        course.getTasks().add(created);
        courseRepo.save(course);
        return true;
    }

    @Override
    public boolean courseExists(String course_code) {
        return courseRepo.existsByCourseCode(course_code);
    }

/*
    @Override
    public boolean createCourse(Course course) {
        courseRepo.saveAndFlush(course);

        return true;
    }
*/

    @Override
    @Transactional
    public boolean createCourse(Course course) {
        if (course.getDepartment() != null) {
            String departmentName = course.getDepartment().getName();

            Department department = departmentRepo.findDepartmentByName(departmentName)
                    .orElseThrow(() -> new RuntimeException("Department not found: " + departmentName));

            course.setDepartment(department); // attach the existing department
        }

        courseRepo.saveAndFlush(course);
        return true;
    }

    @Override
    public Course_DTO findCourse(String course_code) {
        Optional<Course> courseOpt = courseRepo.findCourseByCourseCode(course_code);

        if (!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);

        Course course = courseOpt.get();
        return createDTO(course);
    }

    private Course_DTO createDTO(Course course){
        Course_DTO dto = new Course_DTO();
        dto.setCourse_code(course.getCourseCode());
        dto.setDepartment(course.getDepartment().getName());
        dto.setAcademical_status(course.getCourseAcademicStatus().toString());
        Instructor_DTO coor_dto = new Instructor_DTO();
        dto.setCoordinator(coor_dto);
        dto.setInstructors(null);
        dto.setPrereqs(course.getPrereqList().trim().split("\\s*,\\s*"));
        List<Student_DTO> studDtos = new ArrayList<>();
        for (Student stud : course.getStudentsList()){
            Student_DTO stud_dto = new Student_DTO(stud.getStudentId(),stud.getStudentName(), stud.getStudentSurname());  //i changed the int to long
            studDtos.add(stud_dto);
        }
        dto.setStudents(studDtos);
        List<Section_DTO> sections = new ArrayList<>();
        for (Section section : course.getSectionsList()){
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
        for (TA ta : course.getCourseTas()){
            List<String> courses = new ArrayList<>();
            for (Course c : ta.getCourses()){
                courses.add(c.getCourseCode());
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
        Optional<Course> courseOpt = courseRepo.findCourseByCourseCode(course_code);
        if (!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);

        Course course = courseOpt.get();

        TA ta = taServ.getTAById(ta_id);

        if (ta.getCourses().contains(course))
                throw new GeneralExc("TA with id " + ta_id + " is already assigned to the Course with code " + course_code);

        for (Section sec : ta.getTas_own_lessons()){
            if (sec.getCourse().getCourseCode().equals(course.getCourseCode()))
                throw new GeneralExc("TA with id " + ta_id + " can not be assigned as the TA to the Course with code " + course_code + ". TA takes this course as the lesson.");
        }

        course.getCourseTas().add(ta);
        courseRepo.save(course);

        courseOpt = courseRepo.findCourseByCourseCode(course_code);
        if (!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);

        Course course1 = courseOpt.get();

        if (!course1.getCourseTas().contains(ta))
            throw new NoPersistExc("Assignment ");

        return true;
    }

    @Override
    public List<Course_DTO> getCourses(){
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

    /*
    1.	Department name (String) → row.getCell(0)
	2.	Course number (Numeric) → row.getCell(1)
	3.	Course name (String) → row.getCell(2)
	4.	Prerequisite list (String, optional) → row.getCell(3)
     */
    @Override
    public Map<String, Object> importCoursesFromExcel(MultipartFile file) throws IOException {
        List<Course> successfulCourses = new ArrayList<>();
        List<FailedRowInfo> failedRows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String department = row.getCell(0).getStringCellValue().trim();
                    Optional<Department> departmentOpt = departmentRepo.findDepartmentByName(department); // hope works
                    if (departmentOpt.isEmpty()) {
                        throw new GeneralExc("Department " + department + " does not exist!");
                    }
                    int courseNo = (int) row.getCell(1).getNumericCellValue();
                    String name = row.getCell(2).getStringCellValue().trim();
                    String code = department + "-" + courseNo;
                    String prereq = row.getCell(3) != null ? row.getCell(3).getStringCellValue().trim() : "";

                    // Read academic level from column 4 (E)
                    String academicLevelStr = row.getCell(4) != null ? row.getCell(4).getStringCellValue().trim().toUpperCase() : "BS";
                    AcademicLevelType academicLevel;
                    switch (academicLevelStr) {
                        case "BS":
                            academicLevel = AcademicLevelType.BS;
                            break;
                        case "MS":
                            academicLevel = AcademicLevelType.MS;
                            break;
                        case "PHD":
                            academicLevel = AcademicLevelType.PHD;
                            break;
                        default:
                            throw new GeneralExc("Invalid academic level: " + academicLevelStr + ". Must be BS, MS, or PHD.");
                    }

                    // Validate course code format
                    // id belirlemedim ama

                    Optional<Course> existing = courseRepo.findCourseByCourseCode(code);
                    if (existing.isPresent()) {
                        throw new GeneralExc("Course already exists with department and code: " + code);
                    }
                    Course course = new Course();
                    //course.setCourseId(courseId);
                    course.setCourseCode(code);
                    course.setCourseName(name);
                    course.setDepartment(departmentOpt.get()); // is this correct
                    course.setPrereqList(prereq);
                    course.setCourseAcademicStatus(academicLevel);

                    successfulCourses.add(course);

                } catch (Exception e) {
                    StringBuilder rawData = new StringBuilder();
                    row.forEach(cell -> rawData.append(cell.toString()).append(" | "));
                    failedRows.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage() //hope it is correct error message
                    ));
                }
            }
        }

        if (!successfulCourses.isEmpty()) {
            courseRepo.saveAll(successfulCourses);
            courseRepo.flush();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successfulCourses.size());
        result.put("failedCount", failedRows.size());
        result.put("failedRows", failedRows);
        return result;
    }

    private void checkPrerequisites(Course course) {
        if (course.getPrereqList() != null) {
            String[] prereqs = course.getPrereqList().split(",");
            for (String course_code : prereqs){
                //int id = new CourseCodeConverter().code_to_id(course_code);
                if (courseRepo.existsByCourseCode(course_code)) {
                    throw new NoPrereqCourseFound(course_code);
                }
            }
        }
    }


}
