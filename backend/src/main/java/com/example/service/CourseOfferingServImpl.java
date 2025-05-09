// com/example/service/CourseOfferingServiceImpl.java
package com.example.service;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.dto.CourseOfferingDto;
import com.example.dto.ExamDto;
import com.example.dto.StudentMiniDto;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Section;
import com.example.entity.Exams.Exam;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Student;
import com.example.entity.General.Term;
import com.example.exception.GeneralExc;
import com.example.exception.NoPersistExc;
import com.example.mapper.CourseOfferingMapper;
import com.example.repo.ClassRoomRepo;
import com.example.repo.CourseOfferingRepo;
import com.example.repo.ExamRepo;
import com.example.repo.StudentRepo;
import com.example.repo.TARepo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseOfferingServImpl implements CourseOfferingServ {
    
    
    private final CourseOfferingRepo repo;
    private final SemesterServ semesterServ;
    private final CourseOfferingMapper courseMapper;
    private final ClassRoomRepo classRoomRepo;
    private final TARepo taRepo;
    private final StudentRepo studentRepo;
    private final ExamRepo examRepo;

    @Override
    public CourseOfferingDto getCourseByCourseCode(String code) {
        CourseOffering off = repo.findByCourseCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + code));

        return courseMapper.toDto(off);
    }
    @Override
    public List<CourseOfferingDto> getCoursesByCourseCode(String code) {
        List<CourseOffering> off = repo.findByCoursesCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + code));

        return off.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }
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
    /* public List<CourseOfferingDto> getByCourseCode(String code) {
        List<CourseOffering> off = repo.findByCourseCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + code));

        return off.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    } */

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
        switch (month) {
            // February → June  = SPRING
            case FEBRUARY:
            case MARCH:
            case APRIL:
            case MAY:
            case JUNE:
                return Term.SPRING;

            // July & August = SUMMER
            case JULY:
            case AUGUST:
                return Term.SUMMER;

            // September → January = FALL
            default:
                // (covers SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER, JANUARY)
                return Term.FALL;
        }
    }
//    private Term determineTerm(Month month) {
//        if (month.getValue() >= Month.MARCH.getValue() &&
//            month.getValue() <= Month.MAY.getValue()) {
//            return Term.SPRING;
//        }
//        else if (month.getValue() >= Month.JUNE.getValue() &&
//                 month.getValue() <= Month.AUGUST.getValue()) {
//            return Term.SUMMER;
//        }
//        else {
//            // September–February (incl. December, January, February) → FALL
//            return Term.FALL;
//        }
//    }

    @Transactional
    @Async("setExecutor")
    @Override
    public CompletableFuture<Boolean> createExam(ExamDto dto, String courseCode) {
        CourseOffering offering = getCurrentOffering(courseCode);
        if (offering == null) {
            throw new GeneralExc("No current offering found for course: " + courseCode);
        }
        Exam exam = new Exam();
        exam.setDuration(dto.getDuration());
        exam.setDescription(dto.getType());
        exam.setRequiredTAs(dto.getRequiredTas());
        exam.setWorkload(dto.getWorkload());
        List<StudentMiniDto> studentsAndTas = getSortedListOfStudentsAndTas(offering);
        List<ExamRoom> examRooms = findAndAssignToTheExamRooms(dto.getExamRooms(), studentsAndTas, exam);
        exam.setExamRooms(examRooms);
        offering.getExams().add(exam);
        exam.setCourseOffering(offering);
        examRepo.save(exam);
        int size = offering.getExams().size();
        CourseOffering savedOffering = repo.save(offering);
        if (savedOffering.getExams().size() != size) {
            throw new NoPersistExc("Exam creation");
        }
        return CompletableFuture.completedFuture(true);
    }

    private List<StudentMiniDto> getSortedListOfStudentsAndTas(CourseOffering offering) {
        List<StudentMiniDto> studentsAndTas = getStudentsDto(offering);
        return studentsAndTas.stream()
                    .sorted(
                        Comparator
                        .comparing(StudentMiniDto::getSurname)    // primary key: surname
                        .thenComparing(StudentMiniDto::getName)    // secondary key: name
                    )
                    .collect(Collectors.toList());
    }

    private List<StudentMiniDto> getStudentsDto(CourseOffering offering) {
        List<StudentMiniDto> studentsDto = new ArrayList<>();
        for (Student student : offering.getRegisteredStudents()) {
            StudentMiniDto dto = new StudentMiniDto();
            dto.setId(student.getStudentId());
            dto.setName(student.getStudentName());
            dto.setSurname(student.getStudentSurname());
            studentsDto.add(dto);
        }
        for(TA ta : offering.getRegisteredTas()) {
            StudentMiniDto dto = new StudentMiniDto();
            dto.setId(ta.getId());
            dto.setName(ta.getName());
            dto.setSurname(ta.getSurname());
            dto.setIsTa(true);
            studentsDto.add(dto);
        }
        return studentsDto;
    }

    private List<ExamRoom> findAndAssignToTheExamRooms(List<String> examRoomsDto, List<StudentMiniDto> students, Exam exam) {
        List<ExamRoom> examRooms = new ArrayList<>();
        int c = 0 ;
        for (String code : examRoomsDto){
            ExamRoom examRoom = new ExamRoom();
            examRoom.setExam(exam);
            ClassRoom classRoom = classRoomRepo.findClassRoomByClassroomId(code)
                    .orElseThrow(() -> new GeneralExc("Classroom not found: " + code));
            for (ExamRoom examroom : classRoom.getExamRooms()) {
                if (examroom.getExam().getDuration().has(exam.getDuration())) {
                    throw new GeneralExc("Classroom " + code + " already assigned to an exam for that time " + exam.getDuration());
                }
            }
            for(int i = 0; i < classRoom.getExamCapacity() && i < students.size(); i++) {
                final int index = i; 
                final StudentMiniDto currentStudent = students.get(index);
                if (students.get(index).getIsTa()) {
                    TA ta = taRepo.findById(currentStudent.getId()).orElseThrow(() -> new GeneralExc("TA not found: " + currentStudent.getId()));
                    examRoom.getTasAsStudentsList().add(ta);
                }
                else{
                    Student student = studentRepo.findById(students.get(index).getId()).orElseThrow(() -> new GeneralExc("Student not found: " + students.get(index).getId()));
                    examRoom.getStudentsList().add(student);
                }
                c++;
            }
            examRoom.setExamRoom(classRoom);
            examRooms.add(examRoom);
            if (c == students.size()) {
                return examRooms;
            }
        }
        return examRooms;
    }

    @Transactional
    @Async("setExecutor")
    @Override
    public CompletableFuture<Boolean> addTAs(String courseCode, Integer examId, List<Long> tas) throws GeneralExc {
        CourseOffering offering = getCurrentOffering(courseCode);
        if (offering == null) {
            throw new GeneralExc("No current offering found for course: " + courseCode);
        }
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new GeneralExc("Exam not found: " + examId));
        if(tas.size() > exam.getRequiredTAs() || Objects.equals(exam.getRequiredTAs(), exam.getAmountOfAssignedTAs())) {
            throw new GeneralExc("Number of TAs exceeds the required number for this exam.");
        }
        List<TA> tasList = new ArrayList<>();
        for (Long i : tas) {
            TA ta = taRepo.findById(i)
                    .orElseThrow(() -> new GeneralExc("TA not found: " + i));
            tasList.add(ta);
        } 
        Integer prevAmount = exam.getAmountOfAssignedTAs();
        exam.setAmountOfAssignedTAs(exam.getAmountOfAssignedTAs() + tas.size());
        exam.setAssignedTas(tasList);
        Exam checkExam = examRepo.save(exam);
        if (checkExam.getAmountOfAssignedTAs() != prevAmount + tas.size()) {
            throw new GeneralExc("TA assignment to exam failed."); // Ensure GeneralExc is correctly imported
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Section getSectionByNumber(String courseCode, int sectionNumber) {
        // 1) fetch the “current” offering exactly the way you already do
        CourseOffering offering = getCurrentOffering(courseCode);
    
        // 2) look through its sections and split the sectionCode on “-”
        return offering.getSections().stream()
            .filter(s -> {
                // e.g. "CS‑319‑1‑2025‑SPRING".split("-") → ["CS","319","1","2025","SPRING"]
                String[] parts = s.getSectionCode().split("-");
                // parts[2] is the “1” or “2” that you want
                return parts.length >= 3
                    && Integer.parseInt(parts[2]) == sectionNumber;
            })
            .findFirst()
            .orElseThrow(() ->
                new GeneralExc(
                    "No section “" + sectionNumber +
                    "” for course “" + courseCode + "”"
                )
            );
    }
}