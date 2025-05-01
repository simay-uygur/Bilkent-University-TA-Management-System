package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.example.ExcelHelpers.FailedRowInfo;
import com.example.dto.CourseDto;
import com.example.dto.SectionDto;
import com.example.dto.LessonDto;
import com.example.dto.InstructorDto;
import com.example.dto.TaDto;
import com.example.dto.StudentDto;
import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Department;
import com.example.entity.Courses.Section;
import com.example.entity.Tasks.Task;
import com.example.entity.General.AcademicLevelType;
import com.example.exception.Course.CourseNotFoundExc;
import com.example.exception.Course.NoPrereqCourseFound;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.repo.CourseRepo;
import com.example.repo.DepartmentRepo;
import com.example.repo.SectionRepo;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class CourseServImpl implements CourseServ {

    private final CourseRepo courseRepo;
    private final TAServ taServ;
    private final TaskServ taskServ;
    private final SectionRepo secRepo;
    private final DepartmentRepo departmentRepo;

    @Override
    public boolean addSection(String courseCode, Section section) {
        Course course = courseRepo.findCourseByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundExc(courseCode));
        if (course.getSectionsList() == null) {
            course.setSectionsList(new ArrayList<>());
        }
        course.getSectionsList().add(section);
        secRepo.saveAndFlush(section);
        courseRepo.saveAndFlush(course);
        return true;
    }

    @Override
    public boolean addTask(String courseCode, Task task) {
        Course course = courseRepo.findCourseByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundExc(courseCode));
        task.setCourse(course);
        Task created = taskServ.createTask(task);
        course.getTasks().add(created);
        courseRepo.save(course);
        return true;
    }

    @Override
    public boolean courseExists(String courseCode) {
        return courseRepo.existsByCourseCode(courseCode);
    }

    @Override
    public boolean createCourse(Course course) {
        if (course.getDepartment() != null) {
            String deptName = course.getDepartment().getName();
            Department dept = departmentRepo.findDepartmentByName(deptName)
                    .orElseThrow(() -> new RuntimeException("Department not found: " + deptName));
            course.setDepartment(dept);
        }
        courseRepo.saveAndFlush(course);
        return true;
    }

    @Override
    public CourseDto findCourse(String courseCode) {
        Course course = courseRepo.findCourseByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundExc(courseCode));
        return mapToDto(course);
    }

    @Override
    public List<CourseDto> getCourses() {
        return courseRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

/*

    private CourseDto mapToDto(Course course) {
        CourseDto dto = new CourseDto();

        // Basic info
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setCourseAcademicStatus(course.getCourseAcademicStatus().name());
        dto.setDepartment(course.getDepartment().getName());

//        // Coordinator
//        Instructor coord = course.getCoordinator();
//        if (coord != null) {
//            dto.setCoordinator(new InstructorDto(
//                    coord.getInstructorId(),
//                    coord.getName(),
//                    coord.getSurname(),
//                    coord.getWebmail(),
//                    coord.getRole().name(),
//                    coord.getDepartment().getName()
//            ));
//        }

        // Instructors
        dto.setInstructors(course.getInstructors().stream()
                .map(i -> new InstructorDto(
                        i.getInstructorId(), i.getName(), i.getSurname(),
                        i.getWebmail(), i.getRole().name(), i.getDepartment().getName()
                ))
                .collect(Collectors.toList()));

        // Prerequisites
        dto.setPrereqs(Arrays.stream(course.getPrereqList().split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList()));

        // Students
        dto.setStudents(course.getStudentsList().stream()
                .map(s -> new StudentDto(
                        s.getStudentId(),
                        s.getStudentName(),
                        s.getStudentSurname(),
                        s.getWebmail(),
                        s.getAcademicStatus(),
                        s.getDepartment(),
                        s.getIsActive(),
                        s.getIsGraduated()
                ))
                .collect(Collectors.toList()));

        // Sections & Lessons
        dto.setSections(course.getSectionsList().stream()
                .map(sec -> {
                    List<LessonDto> lessons = sec.getLessons().stream()
                            .map(l -> new LessonDto(
                                    l.getDuration().toString(),
                                    l.getLesson_room().getClass_code()
                            ))
                            .collect(Collectors.toList());
                    return new SectionDto(
                            sec.getSectionId(),
                            sec.getSectionCode(),
                            lessons
                    );
                })
                .collect(Collectors.toList()));

        // TAs
        dto.setTas(course.getCourseTas().stream()
                .map(ta -> new TaDto(
                        ta.getName(),
                        ta.getSurname(),
                        ta.getId(),
                        ta.getAcademic_level().name(),
                        ta.getTotal_workload(),
                        ta.getCourses().stream()
                                .map(Course::getCourseCode)
                                .collect(Collectors.toList()),
                        ta.getTas_own_lessons().stream()
                                .map(Section::getSectionCode)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList()));

        return dto;
    }
*/

    private CourseDto mapToDto(Course course) {
        CourseDto dto = new CourseDto();

        /* ── basic course fields ────────────────────────── */
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setCourseAcademicStatus(course.getCourseAcademicStatus().name());
        dto.setDepartment(course.getDepartment().getName());

        /* ── prereqs (split → List<String>) ─────────────── */
        dto.setPrereqs(
                Arrays.stream(course.getPrereqList().split("\\s*,\\s*"))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList())
        );

        /* ── students ───────────────────────────────────── */
        dto.setStudents(
                course.getStudentsList().stream()
                        .map(s -> new StudentDto(
                                s.getStudentId(),
                                s.getStudentName(),
                                s.getStudentSurname(),
                                s.getWebmail(),
                                s.getAcademicStatus(),      // ← enum → String
                                s.getDepartment(),       // ← entity → name
                                s.getIsActive(),
                                s.getIsGraduated()
                        ))
                        .collect(Collectors.toList())
        );


        dto.setSections(
                course.getSectionsList().stream()
                        .map(sec -> {

                            /* lessons */
                            List<LessonDto> lessons = sec.getLessons().stream()
                                    .map(l -> new LessonDto(
                                            l.getDuration().toString(),
                                            l.getLessonRoom().getClassCode()))
                                    .collect(Collectors.toList());

                            /* instructor + its courses */
                            Instructor i = sec.getInstructor();
                            List<String> courseCodes = i.getCourses().stream()
                                    .map(Course::getCourseCode)
                                    .collect(Collectors.toList());

                            InstructorDto instrDto = new InstructorDto(
                                    i.getId(),                       // id
                                    i.getName(),                     // name
                                    i.getSurname(),                  // surname
                                    i.getWebmail(),                  // webmail
                                    i.getDepartment().getName(),     // department name
                                    courseCodes                      // course codes list
                            );

                            return new SectionDto(
                                    sec.getSectionId(),
                                    sec.getSectionCode(),
                                    lessons,
                                    instrDto
                            );
                        })
                        .collect(Collectors.toList())
        );
        /*
          private Long id;
    private String name;
    private String surname;
    private String academicLevel;
    private int totalWorkload;
    private Boolean isActive;
    private Boolean isGraduated;
    private String department;
    private List<String> courses;
    private List<String> lessons;
    */
        dto.setTas(
                course.getCourseTas().stream()
                        .map(ta -> new TaDto(
                                ta.getId(),
                                ta.getName(),
                                ta.getSurname(),
                                ta.getAcademicLevel().toString(),           // ← camel-case
                                ta.getTotalWorkload(),
                                ta.getIsActive(),
                                ta.getIsGraduated(),// ← camel-case
                                ta.getDepartment(),
                                ta.getCourses().stream()
                                        .map(Course::getCourseCode)
                                        .collect(Collectors.toList()),
                                ta.getTasOwnLessons().stream()          // ← camel-case
                                        .map(Section::getSectionCode)
                                        .collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList())
        );

        return dto;
    }

    @Override
    public boolean assignTA(Long taId, String courseCode) {
        Course course = courseRepo.findCourseByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundExc(courseCode));
        TA ta = taServ.getTAById(taId);
        if (ta.getCourses().contains(course)) {
            throw new GeneralExc("TA " + taId + " already assigned to " + courseCode);
        }
        if (ta.getTasOwnLessons().stream()
                .anyMatch(sec -> sec.getCourse().getCourseCode().equals(courseCode))) {
            throw new GeneralExc("TA " + taId + " takes this course as a student");
        }
        course.getCourseTas().add(ta);
        courseRepo.save(course);
        return true;
    }

    @Override
    public Task getTaskByID(String courseCode, int taskId) {
        if (!courseRepo.existsByCourseCode(courseCode)) {
            throw new CourseNotFoundExc(courseCode);
        }
        return courseRepo.findTask(taskId, courseCode)
                .orElseThrow(() -> new GeneralExc("Task not assigned to " + courseCode));
    }

    @Override
    public boolean updateTask(String courseCode, int taskId, Task task) {
        if (taskServ.updateTask(taskId, task)) {
            return true;
        }
        throw new NoPersistExc("Task update failed");
    }

    @Override
    public Map<String, Object> importCoursesFromExcel(MultipartFile file) throws IOException {
        List<Course> success = new ArrayList<>();
        List<FailedRowInfo> failed = new ArrayList<>();
        try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                try {
                    String deptName = row.getCell(0).getStringCellValue().trim();
                    Department dept = departmentRepo.findDepartmentByName(deptName)
                            .orElseThrow(() -> new GeneralExc("Dept not found: " + deptName));
                    int no = (int) row.getCell(1).getNumericCellValue();
                    String name = row.getCell(2).getStringCellValue().trim();
                    String code = deptName + "-" + no;
                    String prereq = row.getCell(3) != null
                            ? row.getCell(3).getStringCellValue().trim() : "";
                    String acs = row.getCell(4) != null
                            ? row.getCell(4).getStringCellValue().trim().toUpperCase() : "BS";
                    AcademicLevelType alt;
                    switch (acs) {
                        case "BS": alt = AcademicLevelType.BS; break;
                        case "MS": alt = AcademicLevelType.MS; break;
                        case "PHD": alt = AcademicLevelType.PHD; break;
                        default: throw new GeneralExc("Invalid academic level: " + acs);
                    }
                    if (courseRepo.existsByCourseCode(code)) {
                        throw new GeneralExc("Already exists: " + code);
                    }
                    Course c = new Course();
                    c.setCourseCode(code);
                    c.setCourseName(name);
                    c.setDepartment(dept);
                    c.setPrereqList(prereq);
                    c.setCourseAcademicStatus(alt);
                    success.add(c);
                } catch (Exception ex) {
                    StringBuilder raw = new StringBuilder();
                    row.forEach(cell -> raw.append(cell.toString()).append("|"));
                    failed.add(new FailedRowInfo(row.getRowNum(), ex.getClass().getSimpleName() + ": " + ex.getMessage()));
                }
            }
        }
        if (!success.isEmpty()) {
            courseRepo.saveAll(success);
            courseRepo.flush();
        }
        Map<String,Object> res = new HashMap<>();
        res.put("successCount", success.size());
        res.put("failedCount", failed.size());
        res.put("failedRows", failed);
        return res;
    }

    private void checkPrerequisites(Course c) {
        if (c.getPrereqList() != null) {
            for (String code : c.getPrereqList().split(",")) {
                if (!courseRepo.existsByCourseCode(code.trim())) {
                    throw new NoPrereqCourseFound(code.trim());
                }
            }
        }
    }
}



/*
package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.ExcelHelpers.FailedRowInfo;
import com.example.dto.*;
import com.example.entity.Actors.Instructor;
import com.example.entity.Courses.*;
import com.example.entity.General.AcademicLevelType;
import com.example.exception.Course.NoPrereqCourseFound;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
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

*/
/*
    @Override
    public boolean createCourse(Course course) {
        courseRepo.saveAndFlush(course);

        return true;
    }
*//*


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
    public CourseDto findCourse(String course_code) {
        Optional<Course> courseOpt = courseRepo.findCourseByCourseCode(course_code);

        if (!courseOpt.isPresent())
            throw new CourseNotFoundExc(course_code);

        Course course = courseOpt.get();
        return createDTO(course);
    }


    private CourseDto createDTO(Course course) {
        CourseDto dto = new CourseDto();

        // Basic course info
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setCourseAcademicStatus(course.getCourseAcademicStatus().name());
        dto.setDepartment(course.getDepartment().getName());

        // Coordinator
//        Instructor coord = course.getCoordinator();
//        InstructorDto coordDto = new InstructorDto(
//                coord.getInstructorId(),
//                coord.getName(),
//                coord.getSurname(),
//                coord.getWebmail(),
//                coord.getRole().name(),
//                coord.getDepartment().getName()
//        );
        dto.setCoordinator(coordDto);

        // Instructors
        List<InstructorDto> instructorDtos = course.get().stream()
                .map(i -> new InstructorDto(
                        i.getInstructorId(),
                        i.getName(),
                        i.getSurname(),
                        i.getWebmail(),
                        i.getRole().name(),
                        i.getDepartment().getName()
                ))
                .collect(Collectors.toList());
        dto.setInstructors(instructorDtos);

        // Prerequisites (List<String>)
        List<String> prereqs = Arrays.stream(course.getPrereqList().split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        dto.setPrereqs(prereqs);

        // Students
        List<StudentDto> studDtos = course.getStudentsList().stream()
                .map(s -> new StudentDto(
                        s.getStudentId(),
                        s.getStudentName(),
                        s.getStudentSurname(),
                        s.getWebmail(),
                        s.getAcademicStatus().name(),
                        s.getDepartment().getName(),
                        s.getIsActive(),
                        s.getIsGraduated()
                ))
                .collect(Collectors.toList());
        dto.setStudents(studDtos);

        // Sections and their Lessons
        List<SectionDto> sectionDtos = course.getSectionsList().stream()
                .map(sec -> {
                    List<LessonDto> lessonDtos = sec.getLessons().stream()
                            .map(l -> new LessonDto(
                                    l.getDuration().toString(),
                                    l.getLesson_room().getClass_code()
                            ))
                            .collect(Collectors.toList());
                    return new SectionDto(
                            sec.getSection_id(),
                            sec.getSectionCode(),
                            lessonDtos
                    );
                })
                .collect(Collectors.toList());
        dto.setSections(sectionDtos);

        // TAs
        List<TaDto> taDtos = course.getCourseTas().stream()
                .map(ta -> {
                    List<String> taCourseCodes = ta.getCourses().stream()
                            .map(Course::getCourseCode)
                            .collect(Collectors.toList());
                    List<String> taLessonCodes = ta.getTasOwnLessons().stream()
                            .map(Section::getSectionCode)
                            .collect(Collectors.toList());
                    return new TaDto(
                            ta.getName(),
                            ta.getSurname(),
                            ta.getId(),
                            ta.getAcademicLevel().name(),
                            ta.getTotalWorkload(),
                            taCourseCodes,
                            taLessonCodes
                    );
                })
                .collect(Collectors.toList());
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
    public List<CourseDto> getCourses(){
        List<Course> courses = courseRepo.findAll();
        List<CourseDto> coursesDtos = new ArrayList<>();
        for (Course course : courses){
            CourseDto courseDto = createDTO(course);
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

    */
/*
    1.	Department name (String) → row.getCell(0)
	2.	Course number (Numeric) → row.getCell(1)
	3.	Course name (String) → row.getCell(2)
	4.	Prerequisite list (String, optional) → row.getCell(3)
     *//*

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
*/
