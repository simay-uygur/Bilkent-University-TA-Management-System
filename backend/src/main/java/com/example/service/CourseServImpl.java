package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Department;
import com.example.entity.Courses.Section;
import com.example.entity.Tasks.Task;
import com.example.entity.General.AcademicLevelType;
import com.example.exception.Course.CourseNotFoundExc;
import com.example.exception.Course.NoPrereqCourseFound;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.mapper.CourseMapper;
import com.example.mapper.CourseOfferingMapper;
import com.example.mapper.LessonMapper;
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
    private final CourseOfferingMapper offeringMapper;
    private final LessonMapper lessonMapper;
    private final CourseMapper courseMapper;

    @Override
    public Boolean deleteCourse(String courseCode) {
        Course course = courseRepo.findCourseByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundExc(courseCode));
        
        courseRepo.delete(course);
        return true;
    }
    @Override
    public List<CourseDto> getCoursesByDepartment(String deptName) {
        List<Course> courses = courseRepo.findCourseByDepartmentName(deptName)
                                         .orElse(Collections.emptyList());
        return courses.stream()
                      .map(courseMapper::toDto)
                      .collect(Collectors.toList());
    }
    @Override
    public boolean addSection(String courseCode, Section section) {
        Course course = courseRepo.findCourseByCourseCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundExc(courseCode));

        if (course.getCourseOfferings() == null || course.getCourseOfferings().isEmpty()) {
            throw new GeneralExc("No offering found for course: " + courseCode);
        }

        // Assuming latest offering gets the section (you can change this logic)
        CourseOffering offering = course.getCourseOfferings().get(0);
        if (offering.getSections() == null) {
            offering.setSections(new ArrayList<>());
        }

        offering.getSections().add(section);
        section.setOffering(offering); // maintain bidirectional link
        secRepo.saveAndFlush(section);
        courseRepo.saveAndFlush(course);
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
        return courseMapper.toDto(course);
    }

    @Override
    public List<CourseDto> getCourses() {
        return courseRepo.findAll().stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());

        //return mapToDtoList(courseRepo.findAll());
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

/*
    private CourseDto mapToDto(Course course) {
        CourseDto dto = new CourseDto();

        */
    /* ── basic course fields ────────────────────────── *//*
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setCourseAcademicStatus(course.getCourseAcademicStatus().name());
        dto.setDepartment(course.getDepartment().getName());

        *//* ── prereqs (split → List<String>) ─────────────── *//*
        dto.setPrereqs(
                Arrays.stream(course.getPrereqList().split("\\s*,\\s*"))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList())
        );

        *//* ── students ───────────────────────────────────── *//*
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
        ); */


        // inside CourseServImpl.mapToDto(...)
       /*  dto.setSections(
                course.getCourseOfferings().stream()
                        .flatMap(offering -> offering.getSections().stream())
                        .map(sec -> {
                            List<LessonDto> lessons = sec.getLessons().stream()
                                    .map(lessonMapper::toDto)
                                    .collect(Collectors.toList());

                            // — instructor
                            Instructor i = sec.getInstructor();
                            InstructorDto instrDto = new InstructorDto(
                                    i.getId(),
                                    i.getName(),
                                    i.getSurname(),
                                    i.getWebmail(),
                                    i.getDepartment().getName(),
                                    i.getCourses().stream()
                                            .map(Course::getCourseCode)
                                            .collect(Collectors.toList())
                            );

                            return new SectionDto(
                                    sec.getSectionId(),                                  // now Long
                                    sec.getSectionCode(),
                                    lessons,
                                    instrDto
                            );
                        })
                        .collect(Collectors.toList())                             // use Collectors.toList()
        );
        *//*
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
    *//*
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
        ); *//* 

        return dto;
    }*/


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

                    String prereqRaw = row.getCell(3) != null
                        ? row.getCell(3).getStringCellValue().trim() : "";
                String prereq = cleanPrerequisites(prereqRaw);
                    /* String prereq = row.getCell(3) != null
                            ? row.getCell(3).getStringCellValue().trim() : ""; */
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
   /**
 * Processes a prerequisite expression by:
 * 1. Keeping parenthesized groups intact
 * 2. Converting top-level "and" operators to commas
 * 3. Preserving "or" relationships within groups
 * 
 * Example: "(CS 102 or CS 114) and (MATH 225 or MATH 220)"
 * becomes "(CS 102 or CS 114), (MATH 225 or MATH 220)"
 */
private String cleanPrerequisites(String prereqExpression) {
    if (prereqExpression == null || prereqExpression.isEmpty()) {
        return "";
    }
    
    // Remove any "Prerequisite(s):" prefix if present
    String cleaned = prereqExpression.replaceAll("(?i)Prerequisite\\(s\\):\\s*", "");
    
    // Replace top-level "and" with commas (not those inside parentheses)
    StringBuilder result = new StringBuilder();
    int parenDepth = 0;
    boolean lastCharWasAnd = false;
    
    // First pass: Convert "and" outside parentheses to commas
    for (int i = 0; i < cleaned.length(); i++) {
        char c = cleaned.charAt(i);
        
        if (c == '(') {
            parenDepth++;
            result.append(c);
            lastCharWasAnd = false;
        } 
        else if (c == ')') {
            parenDepth--;
            result.append(c);
            lastCharWasAnd = false;
        }
        else if (parenDepth == 0 && i <= cleaned.length() - 5 && 
                 cleaned.substring(i, i + 5).equalsIgnoreCase(" and ")) {
            result.append(", ");
            i += 4;  // Skip "and" (space already added)
            lastCharWasAnd = true;
        }
        else {
            if (!lastCharWasAnd || c != ' ') { // Avoid extra spaces after "and"
                result.append(c);
            }
            lastCharWasAnd = false;
        }
    }
    
    // Second pass: Validate course codes inside groups by matching them with regex
    String cleanedResult = result.toString();
    Pattern coursePattern = Pattern.compile("[A-Z]{2,5}\\s*\\d{3}[A-Z]?");
    
    // Normalize spaces around operators for readability
    cleanedResult = cleanedResult.replaceAll("\\s+or\\s+", " or ");
    
    return cleanedResult.trim();
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

