// src/main/java/com/example/util/TaAvailabilityChecker.java
package com.example.util;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.entity.Exams.Exam;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.DayOfWeek;
import com.example.entity.Tasks.TaTask;
import com.example.repo.RequestRepos.LeaveRepo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.General.Event;
import com.example.entity.Requests.Leave;

@Component
@RequiredArgsConstructor
public class TaAvailabilityChecker {
    private final LeaveRepo leaveRepo;
    /**
     * Returns true if TA has any Lesson in the same weekday + time‐slot
     * as the given exam duration.
     */
    public boolean hasOverlappingLesson(TA ta, Event event) {
        com.example.entity.General.DayOfWeek dayOfWeek =
                DayOfWeek.valueOf(event.getStart()          // your embeddable Date
                        .toLocalDateTime()  // → LocalDateTime
                        .getDayOfWeek()      // → java.time.DayOfWeek
                        .name());             // → String like "MONDAY"

        com.example.entity.General.DayOfWeek ourDow =
                com.example.entity.General.DayOfWeek.valueOf(String.valueOf(dayOfWeek));

        // 2) scan all lessons on that same weekday
        for (Section section : ta.getSectionsAsStudent()) {
            for (Lesson lesson : section.getLessons()) {
                if (lesson.getDay() != ourDow) {
                    continue; // different weekday → no possible conflict
                }
                // 3) same-day: now just check time overlap
                if (lesson.getDuration().has(event)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasOverlappingExamProctoring(TA ta, Event examDuration) {
        for (Exam exam : ta.getExams()) {
            if ( exam.getDuration().has(examDuration) ) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasOverlappingExam(TA ta, Event examDuration) {
        for (ExamRoom er : ta.getExamRoomsAsStudent()) {
            if ( er.getExam().getDuration().has(examDuration) ) {
                return true;
            }
        }
        return false;
    }


    public static boolean hasOverlappingTask(TA ta, Event examDuration) {
        for (TaTask task : ta.getTaTasks()) {
            if ( task.getTask().getDuration().has(examDuration) ) {
                return true;
            }
        }
        return false;
    }

    public static boolean isActive(TA ta, Event duration){
        List<Leave> reqs = ta.getSendedLeaveRequests();
        for (Leave l : reqs){
            if (l.getDuration().has(duration))
                return false;
        }
        return true;
    }

    public boolean isAvailable(TA ta, Event event) {

        return !hasOverlappingLesson(ta, event)
                && !hasOverlappingExam(ta, event)
                && !hasOverlappingExamProctoring(ta, event)
                && !hasOverlappingTask(ta, event)
                && isActive(ta, event);
    }
}