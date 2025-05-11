package com.example.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Lesson;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.Schedule.ScheduleItemDto;
import com.example.entity.Schedule.ScheduleItemType;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.repo.ExamRoomRepo;
import com.example.repo.LessonRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServImpl implements ScheduleServ {

    private final TARepo       taRepo;
    private final LessonRepo   lessonRepo;
    private final TaTaskRepo   taTaskRepo;
    private final ExamRoomRepo examRoomRepo;

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleItemDto> getWeeklySchedule(Long taId, LocalDateTime weekStart) {
        System.out.println(">> getWeeklySchedule for TA " + taId + " weekStart=" + weekStart);
        TA ta = taRepo.findById(taId)
                      .orElseThrow(() -> new TaNotFoundExc(taId));

        LocalDate monday = weekStart.toLocalDate()
                                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<ScheduleItemDto> schedule = new ArrayList<>();

        // --- LESSONS ---
        List<Lesson> lessons = lessonRepo.findBySection_AssignedTas_Id(taId);
        System.out.println("  lessons found: " + lessons.size());
        for (Lesson l : lessons) {
            Event dur = l.getDuration();
            // only if date parts are set
            
                // compute actual lesson date in this week
                DayOfWeek dow = DayOfWeek.valueOf(l.getDay().name());
                LocalDate lessonDate = monday.with(TemporalAdjusters.nextOrSame(dow));
                Date s = dur.getStart(), f = dur.getFinish();
                schedule = split(schedule,
                      new Date(lessonDate.getDayOfMonth(), lessonDate.getMonthValue(),
                               lessonDate.getYear(), s.getHour(), s.getMinute()),
                      new Date(lessonDate.getDayOfMonth(), lessonDate.getMonthValue(),
                               lessonDate.getYear(), f.getHour(), f.getMinute()),
                      "Lesson",
                      l.getLessonRoom().getClassroomId(),
                      l.getSection().getSectionCode(),
                      l.getLessonId());
            
        }

        // --- TASKS ---
        var tasks = taTaskRepo.findAllByTaId(taId);
        System.out.println("  tasks found: " + tasks.size());
        for (var tt : tasks) {
            Event dur = tt.getTask().getDuration();
            schedule = split(schedule,
                  dur.getStart(), dur.getFinish(),
                  tt.getTask().getTaskType().toString(),
                  "-",
                  tt.getTask().getSection().getSectionCode(),
                  (long) tt.getTask().getTaskId());
        }

        // --- PROCTORINGS ---
        List<ExamRoom> rooms = examRoomRepo.findByAssignedTas_Id(taId);
        System.out.println("  proctorings found: " + rooms.size());
        for (ExamRoom er : rooms) {
            Event dur = er.getExam().getDuration();
            String roomId = er.getExamRoom() != null
                          ? er.getExamRoom().getClassroomId()
                          : "";
                          schedule = split(schedule,
                  dur.getStart(), dur.getFinish(),
                "Proctoring",
                  roomId,
                  er.getExam().getCourseOffering().getCourse().getCourseCode(),
                  (long) er.getExamRoomId());
        }

        // --- SORT & RETURN ---
        schedule.sort(Comparator
            .comparing(ScheduleItemDto::getDate)
            .thenComparingInt(ScheduleItemDto::getSlotIndex));
        System.out.println("  returning " + schedule.size() + " slots");
        return schedule;
    }

    private List<ScheduleItemDto> split(List<ScheduleItemDto> out,
                       Date start, Date end,
                       String type,
                       String classroom, String code,
                       Long refId) {
        if (start.getHour() == null || start.getMinute() == null ||
            end  .getHour() == null || end  .getMinute() == null) return out;

        int startMin = start.getHour() * 60 + start.getMinute();
        int endMin   = end  .getHour() * 60 + end  .getMinute();
        int total    = endMin - startMin;
        if (total <= 0) return out;

        int segments = (int)Math.ceil(total / 60.0);
        for (int i = 0; i < segments; i++) {
            int seg = startMin + i * 50;
            int slot = 1 + ((seg - (8*60 + 30)) / 50);

            ScheduleItemDto dto = new ScheduleItemDto();
            dto.setDate(LocalDate.of(start.getYear(), start.getMonth(), start.getDay()));
            dto.setSlotIndex(slot);
            dto.setType(type);
            dto.setReferenceId(refId);
            if (i == 0) {
                dto.setClassroom(classroom);
                dto.setCode(code);
            }
            out.add(dto);
        }
        return out;
    }

    @Override
    public LocalDate getWeekStart(LocalDate date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWeekStart'");
    }
}
