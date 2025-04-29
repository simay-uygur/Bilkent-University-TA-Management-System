package com.example.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Exams.Exam;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.Date;
import com.example.entity.Tasks.TA_Task;
import com.example.entity.Tasks.Task;
import com.example.exception.GeneralExc;
import com.example.repo.CourseRepo;
import com.example.repo.DepartmentRepo;
import com.example.repo.ExamRepo;
import com.example.repo.TARepo;
import com.example.repo.TA_TaskRepo;
import com.example.repo.TaskRepo;

import lombok.AllArgsConstructor;

/*restrictions:
 - from the same faculty(can be overriden) [0]
 - one day after/before the another proctoring(can be overriden) [1]
 - only phd stud to ms or phd. ms stud only to ms (can be overriden) [2]
 - is not student in the course
 - does not have exams or another task
 - is on leave

 */
@Service
@AllArgsConstructor
public class ProctoringServImpl implements ProctoringServ{

    private final CourseRepo courseRepo;
    private final TaskRepo taskRepo; 
    private final ExamRepo examRepo;
    private final DepartmentRepo depRepo;
    private final TA_TaskRepo taTaskRepo;
    private final TARepo taRepo;

    @Override
    public void autoAssignment(Exam exam) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'autoAssignment'");
    }

    @Override
    public void manualAssignment(Exam exam, boolean[] restrictions) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'manualAssignment'");
    }

    private List<TA> findTAsByRestricions(Exam exam, boolean[] restrictions){
        Task task = exam.getTask();
        Course course = task.getCourse();
        
        List<TA> course_tas = course.getCourseTas();
        List<TA> depTas = new ArrayList<>();
        for(Course c : course.getDepartment().getCourses()){
            depTas.addAll(c.getCourseTas());
        }
        List<TA> filtered_by_date_tas = taRepo.findAllFreeBetween(task.getDuration().getStart(), task.getDuration().getFinish());
        List<TA> filtered_by_acad_level_tas = filterByAcademicLevel(course.getCourseAcademicStatus(), filtered_by_date_tas, restrictions[2]);
        List<TA> filtered_by_availability_tas = findEligibleTAsForExam(task.getDuration().getStart(), filtered_by_acad_level_tas);
        if (filtered_by_availability_tas.size() < task.getRequiredTAs())
            throw new GeneralExc("System could not find enough TAs considering the requirements!");
        
        return filtered_by_availability_tas;
    }

    // checks

    private boolean isAcademicLevelOK(AcademicLevelType course_type, AcademicLevelType ta_type, boolean restriction_enabled){
        if (course_type == AcademicLevelType.BS)
            return true;

        if (course_type == AcademicLevelType.MS)
            if (ta_type == AcademicLevelType.PHD || ta_type == AcademicLevelType.MS)
                return true;
            else if (ta_type == AcademicLevelType.BS && !restriction_enabled)
                return true;

        if (course_type == AcademicLevelType.PHD)
            if (ta_type == AcademicLevelType.PHD)
                return true;
            else if (ta_type == AcademicLevelType.MS && !restriction_enabled)
                return true;
        
        return false;
    }

    private List<TA> filterByAcademicLevel(AcademicLevelType course_type, List<TA> tas, boolean restriction_enabled){
        return tas.stream()
        .filter(ta -> isAcademicLevelOK(
            course_type,
            ta.getAcademic_level(),         
            restriction_enabled
        ))
        .collect(Collectors.toList());
    }

    private List<TA> findEligibleTAsForExam(Date newExamDate, List<TA> tas) {
        LocalDate examDay = LocalDate.of(
            newExamDate.getYear(),
            newExamDate.getMonth(),
            newExamDate.getDay()
        );
        LocalDate prevDay  = examDay.minusDays(1);
        LocalDate nextDay  = examDay.plusDays(1);

        return tas.stream()
            .filter(ta -> isFreeOfAdjacentExams(ta, examDay, prevDay, nextDay))
            .collect(Collectors.toList());
    }

    private boolean isFreeOfAdjacentExams(
        TA ta,
        LocalDate examDate,
        LocalDate prevDay,
        LocalDate nextDay
    ) {
        return ta.getTa_tasks().stream()
            .map(TA_Task::getTask)                         // :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
            .filter(task -> task.getExam() != null)        // :contentReference[oaicite:2]{index=2}&#8203;:contentReference[oaicite:3]{index=3}
            .map(task -> task.getDuration().getStart())     // get the embeddable Date :contentReference[oaicite:4]{index=4}&#8203;:contentReference[oaicite:5]{index=5}
            // convert to java.time.LocalDate for easy comparison
            .map(d -> LocalDate.of(d.getYear(), d.getMonth(), d.getDay()))
            // if any date equals examDate, prevDay, or nextDay → NOT free
            .noneMatch(d ->
                d.equals(examDate) ||
                d.equals(prevDay)  ||
                d.equals(nextDay)
            );
    }
}
