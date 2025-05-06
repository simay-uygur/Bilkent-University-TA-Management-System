package com.example.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.dto.ExamDto;
import com.example.dto.ProctoringDto;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.Exams.Exam;
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
    @Override
    @Async("setExecutor")
    public CompletableFuture<List<ProctoringDto>> getProctoringInfo(Integer examId, String courseCode) {
        List<ProctoringDto> availableTas = new ArrayList<>();
        Exam exam = examRepo.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));
        Event examDuration = exam.getDuration();
        CourseOffering offering = courseOfferingServ.getCurrentOffering(courseCode);
        //List<TA> tas = offering.getAssignedTas();
        List<TA> tas = taRepo.findAll();
        for (TA ta : tas) {
            if (ta.isActive() && !ta.isDeleted() && 
                !hasAnyDutiesOrLessons(ta, examDuration)) {
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
    
    private boolean hasAnyDutiesOrLessons(TA ta, Event examDuration) {
        // Check if the TA has any duties or lessons that conflict with the exam duration
        for (TaTask task : ta.getTaTasks()) {
            if (task.getTask().getDuration().has(examDuration)) {
                return true;
            }
        }
        for (Section section : ta.getSectionsAsStudent()) {
            for (Lesson lesson : section.getLessons()) {
                if (lesson.getDuration().has(examDuration)) {
                    return true;
                }
            }
        }
        return false;
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
