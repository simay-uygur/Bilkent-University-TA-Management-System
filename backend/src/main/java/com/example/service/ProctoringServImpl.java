package com.example.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.entity.General.DayOfWeek;
import com.example.util.TaAvailabilityChecker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.dto.ProctoringDto;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.Exams.Exam;
import com.example.entity.General.DayOfWeek;
import com.example.entity.General.Event;
import com.example.entity.Tasks.TaTask;
import com.example.repo.ExamRepo;
import com.example.repo.TARepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProctoringServImpl implements ProctoringServ{

    private final CourseOfferingServ courseOfferingServ;
    private final ExamRepo examRepo;
    private final TARepo taRepo;
    private final TaAvailabilityChecker availabilityChecker;
    @Override
    @Async("setExecutor")
    public CompletableFuture<List<ProctoringDto>> getProctoringInfo(Integer examId, String courseCode) {
        List<ProctoringDto> availableTas = new ArrayList<>();
        Exam exam = examRepo.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));
        Event examDuration = exam.getDuration();
        String[] parts = courseCode.split("-");
        List<TA> tas = taRepo.findAllByDepartment(parts[0]);
        //List<TA> tas = taRepo.findAll();
        for (TA ta : tas) {
            if (ta.isActive() && !ta.isDeleted() && 
                availabilityChecker.isAvailable(ta, examDuration)) { // should be checked if it works
                ProctoringDto proctoringDto = new ProctoringDto();
                proctoringDto.setTaId(ta.getId());
                proctoringDto.setName(ta.getName());
                proctoringDto.setSurname(ta.getSurname());
                proctoringDto.setAcademicLevel(ta.getAcademicLevel().toString());
                proctoringDto.setWorkload(ta.getTotalWorkload());
                if(!hasProctoringDayBeforeOrAfter(ta, examDuration))
                    proctoringDto.setHasAdjacentExam(false);
                else 
                    proctoringDto.setHasAdjacentExam(true);
                availableTas.add(proctoringDto);
            }
        }
        return CompletableFuture.completedFuture(availableTas);
    }

    private int getDay(DayOfWeek day){
        switch(day) {
            case MONDAY:
                return 1;
            case TUESDAY:
                return 2;
            case WEDNESDAY:
                return 3;
            case THURSDAY:
                return 4;
            case FRIDAY:
                return 5;
            case SATURDAY:
                return 6;
            case SUNDAY:
                return 7;
            default:
                throw new IllegalArgumentException("Invalid day: " + day);
        }
    }

    private boolean hasProctoringDayBeforeOrAfter(TA ta, Event examDuration) {
        LocalDate newDate = examDuration
                            .getStart()
                               .toLocalDate();  // using the helper above
        for (Exam existing : ta.getExams()) {
            LocalDate existingDate = existing.getDuration()
                                            .getStart()
                                            .toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(existingDate, newDate);
            if (Math.abs(daysBetween) <= 1) {
                // found an exam the day before, the same day, or the day after
                return true;
            }
        }
        return false;
    }
}
