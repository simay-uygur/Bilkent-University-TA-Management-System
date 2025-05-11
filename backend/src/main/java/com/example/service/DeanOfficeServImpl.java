package com.example.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CourseDto;
import com.example.dto.CourseOfferingDto;
import com.example.dto.DeanOfficeDto;
import com.example.dto.ExamDto;
import com.example.dto.FacultyCourseDto;
import com.example.dto.FacultyCourseOfferingsDto;
import com.example.entity.Actors.DeanOffice;
import com.example.entity.Actors.Role;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Department;
import com.example.entity.Exams.Exam;
import com.example.entity.General.Faculty;
import com.example.entity.General.Term;
import com.example.mapper.CourseMapper;
import com.example.mapper.CourseOfferingMapper;
import com.example.mapper.DeanOfficeMapper;
import com.example.repo.CourseOfferingRepo;
import com.example.repo.CourseRepo;
import com.example.repo.DeanOfficeRepo;
import com.example.repo.ExamRepo;
import com.example.repo.FacultyRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeanOfficeServImpl implements DeanOfficeServ {
    private final DeanOfficeMapper deanOfficeMapper;
    private final DeanOfficeRepo deanOfficeRepo;
    private final FacultyRepo facultyRepo;
    private final PasswordEncoder encoder;
    private final CourseOfferingRepo courseOfferingRepo;
    private final CourseMapper courseMapper;
    private final CourseRepo courseRepo;
    private final CourseOfferingMapper courseOfferingMapper;
    private final ExamRepo examRepo;
    private final LogService log;

    @Override
    @Transactional
    public DeanOffice save(DeanOffice deanOffice, String facultyCode) {
        // encode & role
        deanOffice.setPassword(encoder.encode(deanOffice.getPassword()));
        deanOffice.setRole(Role.DEANS_OFFICE);

        // fetch the faculty (owning side)
        Faculty faculty = facultyRepo.findById(facultyCode)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));

        // wire up both sides
        deanOffice.setFaculty(faculty);
        faculty.setDeanOffice(deanOffice);
        log.info("New Dean Office Member is created", "");
        // now saving the faculty will cascade to persist the DeanOffice
        return facultyRepo.save(faculty)
                .getDeanOffice();
    }

    /* @Override
    public List<DeanOffice> getAll() {
        return deanOfficeRepo.findAll();
    } */
    @Override
    public List<DeanOfficeDto> getAll() {
        List<DeanOffice> deanOffices = deanOfficeRepo.findAll();
        return deanOffices.stream()
                .map(deanOfficeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeanOffice getById(Long id) {
        return deanOfficeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DeanOffice not found"));
    }

    @Override
    public void deleteById(Long id) {
        log.info("Dean Office Member is deleted", "");
        deanOfficeRepo.deleteById(id);
    }

    @Override
    public DeanOffice saveFromDto(DeanOfficeDto deanOfficeDto, String facultyCode) {
        Faculty faculty = facultyRepo.findById(facultyCode)
                .orElseThrow(() -> new IllegalArgumentException("…"));
        DeanOffice deanOffice = deanOfficeMapper.toEntity(deanOfficeDto, faculty);
        return deanOfficeRepo.save(deanOffice);
    }
    @Override
    public FacultyCourseDto getFacultynormalCourseData(String facultyCode){
          Faculty faculty = facultyRepo.findById(facultyCode)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found: " + facultyCode));
        List<Department> depts = faculty.getDepartments();

        // 2) figure out “current” term & year
        LocalDate now = LocalDate.now();
        Term   term = determineTerm(now.getMonth());
        int    year = now.getYear();


        // 4) fetch all courses in those same departments (no term-filter here)
        List<Course> courses = courseRepo.findByDepartmentIn(depts);
        List<CourseDto> courseDtos = courses.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());

        return new FacultyCourseDto(courseDtos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public FacultyCourseOfferingsDto getFacultyCourseData(String facultyCode) {
        // 1) load faculty and its departments
        Faculty faculty = facultyRepo.findById(facultyCode)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found: " + facultyCode));
        List<Department> depts = faculty.getDepartments();

        // 2) figure out “current” term & year
        LocalDate now = LocalDate.now();
        Term   term = determineTerm(now.getMonth());
        int    year = now.getYear();

        // 3) fetch only those offerings in our departments _and_ in this term
        List<CourseOffering> offerings = courseOfferingRepo
                .findByCourse_DepartmentInAndSemester_YearAndSemester_Term(depts, year, term);

        // map to DTO
        List<CourseOfferingDto> offeringDtos = offerings.stream()
                .map(courseOfferingMapper::toDto)
                .collect(Collectors.toList());

        // 4) fetch all courses in those same departments (no term-filter here)
        List<Course> courses = courseRepo.findByDepartmentIn(depts);
        List<CourseDto> courseDtos = courses.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());

        return new FacultyCourseOfferingsDto(offeringDtos, courseDtos);
    }

    private Term determineTerm(Month m) {
        return switch (m) {
            case FEBRUARY, MARCH, APRIL, MAY, JUNE -> Term.SPRING;
            case JULY, AUGUST -> Term.SUMMER;
            default -> Term.FALL;
        };
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExamDto> getAllExamsForFaculty(String facultyCode) {
        // 1) fetch faculty → departments
        Faculty faculty = facultyRepo.findById(facultyCode)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found: " + facultyCode));
        List<Department> depts = faculty.getDepartments();

        // 2) determine current term & year
        LocalDate now  = LocalDate.now();
        Term      term = determineTerm(now.getMonth());
        int       year = now.getYear();

        // 3) load this term’s offerings in those departments
        List<CourseOffering> offerings = courseOfferingRepo
                .findByCourse_DepartmentInAndSemester_YearAndSemester_Term(depts, year, term);

        // 4) flatten out every Exam on each offering
        return offerings.stream()
                .flatMap(offering -> offering.getExams().stream())
                .map(exam -> {
                    // reuse your ExamDto constructor:
                    List<String> rooms = exam.getExamRooms().stream()
                            .map(er -> er.getExamRoom().getClassroomId())
                            .collect(Collectors.toList());

                    return new ExamDto(
                            exam.getExamId(),
                            exam.getDuration(),
                            exam.getCourseOffering().getCourse().getCourseCode(),
                            exam.getDescription(),
                            rooms,
                            exam.getRequiredTAs(),
                            exam.getWorkload()
                    );
                })
                .toList();
    }


    @Transactional(readOnly = true)
    @Override
    public ExamDto getExamDetails(Integer examId) {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));

        // map room‐codes
        List<String> rooms = exam.getExamRooms()
                .stream()
                .map(er -> er.getExamRoom().getClassroomId())
                .collect(Collectors.toList());

        return new ExamDto(
                examId,
                exam.getDuration(),
                exam.getCourseOffering().getCourse().getCourseCode(),
                exam.getDescription(),
                rooms,
                exam.getRequiredTAs(),
                exam.getWorkload()
        );
    }

}