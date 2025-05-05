// com/example/service/CourseOfferingServiceImpl.java
package com.example.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.dto.CourseOfferingDto;
import com.example.dto.ExamDto;
import com.example.dto.ExamRoomDto;
import com.example.dto.StudentDto;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Exams.Exam;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Student;
import com.example.entity.General.Term;
import com.example.entity.Tasks.Task;
import com.example.entity.Tasks.TaskAccessType;
import com.example.entity.Tasks.TaskType;
import com.example.exception.GeneralExc;
import com.example.mapper.CourseOfferingMapper;
import com.example.repo.ClassRoomRepo;
import com.example.repo.CourseOfferingRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseOfferingServImpl implements CourseOfferingServ {
    
    private final CourseOfferingRepo repo;
    private final SemesterServ semesterServ;
    private final CourseOfferingMapper courseMapper;
    private final ClassRoomRepo classRoomRepo;

    @Override
    public List<CourseOfferingDto> getOfferingsByDepartment(String deptName){
        List<CourseOffering> offerings = repo.findByCourseDepartmentName(deptName)
                .orElseThrow(() -> new IllegalArgumentException("No offerings found for department: " + deptName));
        
                return offerings.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseOffering create(CourseOffering offering) {
        Long courseId = (long) offering.getCourse().getCourseId();
        Long semesterId = offering.getSemester().getId();

        Optional<CourseOffering> existing = repo.findByCourse_CourseIdAndSemester_Id(courseId, semesterId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Offering already exists for this course and semester.");
        }

        offering.setSemester(semesterServ.getById(semesterId));

        return repo.save(offering);
    }
    @Override
    public CourseOffering update(Long id, CourseOffering offering) {

        CourseOffering existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + id));
        existing.setSemester(offering.getSemester());
        existing.setCourse(offering.getCourse());
        // you could update other fields here
        return repo.save(existing);
    }

    @Override
    public CourseOfferingDto getById(Long id) {
        CourseOffering off = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + id));

        return courseMapper.toDto(off);
    }
    public CourseOfferingDto getByCourseCode(String code) {
        List<CourseOffering> off = repo.findByCourseCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + code));

        return off.stream()
                .map(courseMapper::toDto)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + code));
    }

    @Override
    public List<CourseOffering> getAll() {
        return repo.findAll();
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
    @Override
    public Optional<CourseOffering> getByCourseAndSemester(Long courseId, Long semesterId) {
        return repo.findByCourse_CourseIdAndSemester_Id(courseId, semesterId);
    }

    //this should be written
    @Override
    public boolean assignTA(Long taId, String courseCode) {
        return false;
    }
    //old one - fix needed
//    @Override
//    public boolean assignTA(Long taId, String courseCode) {
//        Course course = repo.findCourseByCourseCode(courseCode)
//                .orElseThrow(() -> new CourseNotFoundExc(courseCode));
//        TA ta = taServ.getTAById(taId);
//        if (ta.getSectionsAsHelper().contains(course)) {
//            throw new GeneralExc("TA " + taId + " already assigned to " + courseCode);
//        }
//        if (ta.get().stream()
//                .anyMatch(sec -> sec.getOffering().getCourse().getCourseCode().equals(courseCode))) {
//            throw new GeneralExc("TA " + taId + " takes this course as a student");
//        }
//        course.getCourseTas().add(ta);
//        courseRepo.save(course);
//        return true;
//    }
//
    @Override
    public CourseOffering getCurrentOffering(String courseCode) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        Term term = determineTerm(today.getMonth());

        return repo.findByCourse_CourseCodeAndSemester_YearAndSemester_Term(
                    courseCode, year, term
               )
               .orElseThrow(() -> new NoSuchElementException(
                   "No offering for " + courseCode +
                   " in " + term + " " + year));
    }

    /** Map month → academic term */
    private Term determineTerm(Month month) {
        if (month.getValue() >= Month.MARCH.getValue() &&
            month.getValue() <= Month.MAY.getValue()) {
            return Term.SPRING;
        }
        else if (month.getValue() >= Month.JUNE.getValue() &&
                 month.getValue() <= Month.AUGUST.getValue()) {
            return Term.SUMMER;
        }
        else {
            // September–February (incl. December, January, February) → FALL
            return Term.FALL;
        }
    }

    @Async("setExecutor")
    @Override
    public void createExam(ExamDto dto, String courseCode) {
        CourseOffering offering = getCurrentOffering(courseCode);
        if (offering == null) {
            throw new GeneralExc("No current offering found for course: " + courseCode);
        }
        Task task = new Task();
        task.setTaskType(TaskType.Proctoring);
        task.setDuration(dto.getDuration());
        task.setRequiredTAs(dto.getRequiredTas());
        task.setAccessType(TaskAccessType.PUBLIC);
        Exam exam = new Exam();
        exam.setTask(task);
        exam.setDescription(dto.getType());
        List<ExamRoom> rooms = new ArrayList<>();
        for (ExamRoomDto roomDto : dto.getExamRooms()) {
            ExamRoom room = new ExamRoom();
            ClassRoom classRoom = classRoomRepo.findByClassroomId(roomDto.getRoom())
                    .orElseThrow(() -> new GeneralExc("Classroom not found: " + roomDto.getRoom()));
            room.setExamRoom(classRoom);
            classRoom.getExamRooms().add(room);
            List<Student> students = new ArrayList<>();
            for (StudentDto studentDto : roomDto.getStudents()) {
                Student student = new Student();
                student.setStudentId(studentDto.getStudentId());
                student.setStudentName(studentDto.getStudentName());
                student.setStudentSurname(studentDto.getStudentSurname());
                students.add(student);
            }
            room.setStudentsList(students);
            rooms.add(room);
            classRoomRepo.save(classRoom);
        }
        offering.getExams().add(exam);
        exam.setCourseOffering(offering);
        repo.save(offering);
    }
}