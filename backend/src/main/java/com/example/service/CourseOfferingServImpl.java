// com/example/service/CourseOfferingServiceImpl.java
package com.example.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
import com.example.repo.ClassRoomRepo;
import com.example.repo.CourseOfferingRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseOfferingServImpl implements CourseOfferingServ {
    private final CourseOfferingRepo repo;
    private final SemesterServ semesterServ;
    private final CourseOfferingServ service;
    private final CourseOfferingRepo courseOfferingRepo;
    private final ClassRoomRepo classRoomRepo;

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
        CourseOffering existing = getById(id);
        existing.setSemester(offering.getSemester());
        existing.setCourse(offering.getCourse());
        // you could update other fields here
        return repo.save(existing);
    }

    @Override
    public CourseOffering getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + id));
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
        CourseOffering offering = service.getCurrentOffering(courseCode);
        if (offering == null) {
            throw new GeneralExc("No current offering found for course: " + courseCode);
        }
        Task task = new Task();
        task.setTaskType(TaskType.Proctoring);
        task.setDuration(dto.getDuration());
        task.setCourse(offering.getCourse());
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
            classRoom.setExamRoom(room);
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
        courseOfferingRepo.save(offering);
    }
}