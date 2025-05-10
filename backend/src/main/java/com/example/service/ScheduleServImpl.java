package com.example.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.hpsf.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;
import com.example.entity.Schedule.ScheduleItemDto;
import com.example.entity.Schedule.ScheduleItemType;
import com.example.entity.Tasks.TaTask;
import com.example.entity.Tasks.Task;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.repo.LessonRepo;
import com.example.repo.TARepo;
import com.example.repo.TaTaskRepo;

import jakarta.persistence.Embeddable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Embeddable
@Service
@RequiredArgsConstructor
public class ScheduleServImpl implements ScheduleServ {

    @FunctionalInterface
    private interface Splitter {
        void split(Date start, Date end, ScheduleItemType type, String classroom, String code, Long refId);
    }
    // Implement the methods defined in the ScheduleServ interface here
    // For example:
    // @Override
    // public void createSchedule() {
    //     // Implementation code here
    // }
    /**
     * Given any date, computes the Monday of that week.
     *
     * @param date any date within the week.
     * @return the Monday of that week.
     */
    private final TARepo taRepo;

    private final TaTaskRepo taTaskRepo;
    private final LessonRepo lessonRepo;

    public LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
    
    @Override
    @Transactional
    public List<ScheduleItemDto> getWeeklySchedule(Long taId, LocalDateTime weekStart){
        TA ta = taRepo.findById(taId)
                      .orElseThrow(() -> new TaNotFoundExc(taId));
        List<ScheduleItemDto> schedule = new ArrayList<>();
        Splitter splitter = (Date start, Date end,
                            ScheduleItemType type, String classroom, String code, Long refId) -> {
            if (start.getHour()   == null || start.getMinute() == null ||
                end .getHour()   == null || end .getMinute()   == null) {
                return;  // incomplete time → skip
            }
                    
            // 1) total minutes since midnight
            int startTotal = start.getHour() * 60 + start.getMinute();
            int endTotal   = end.getHour() * 60 + end.getMinute();
            int totalMinutes = endTotal - startTotal;
            if (totalMinutes <= 0) {
                return;  // no positive duration → skip
            }
                    
                            // 2) how many 50-min slots?
            int segments = (int) Math.ceil(totalMinutes / 50.0);
            for (int i = 0; i < segments; i++) {
                int segStartTotal = startTotal + i * 50;
                int slotIdx = 1 + ((segStartTotal - (8 * 60 + 30)) / 50);
                ScheduleItemDto dto = new ScheduleItemDto();
                if (start.getYear() != null &&
                start.getMonth()!= null &&
                start.getDay()  != null) {
                dto.setDate(LocalDate.of(
                    start.getYear(),
                    start.getMonth(),
                    start.getDay()
                ));
            }
                dto.setSlotIndex(slotIdx);
                dto.setType(type);
                dto.setReferenceId(refId);
                // only on the first segment do we set these
                if (i == 0) {
                    dto.setClassroom(classroom);
                    dto.setCode(code);
                }
                schedule.add(dto);
            }
        };
        // 3a. lessons
        List<Lesson> lessons = lessonRepo.findBySection_AssignedTas_Id(ta.getId());
        for (Lesson l : lessons) {
            var dur = l.getDuration();
            dur.setStart(setDay(dur.getStart(), l.getDay()));
            dur.setFinish(setDay(dur.getStart(), l.getDay()));
            splitter.split(dur.getStart(), dur.getFinish(),
                           ScheduleItemType.LESSON,
                           l.getLessonRoom().getClassroomId(),
                           l.getSection().getSectionCode(),
                           l.getLessonId());
        }

        // 3b. tasks
        List<TaTask> tasks = taTaskRepo.findAllByTaId(ta.getId());
        for (TaTask tt : tasks) {
            var t = tt.getTask();
            var dur = t.getDuration();
            splitter.split(dur.getStart(), dur.getFinish(),
                           ScheduleItemType.TASK,
                           t.getRoom().getClassroomId(),
                           t.getSection().getSectionCode(),
                           (long) t.getTaskId());
        }

        // 3c. proctorings
        for (ExamRoom er : ta.getExamRooms()) {
            var dur = er.getExam().getDuration();
            ClassRoom room = er.getExamRoom();
            splitter.split(dur.getStart(), dur.getFinish(),
                           ScheduleItemType.PROCTORING,
                           room == null ? "" : room.getClassroomId(),
                           er.getExam().getCourseOffering().getCourse().getCourseCode(),
                           (long) er.getExamRoomId());
        }

        // you can sort by date then slotIndex if you like
        schedule.sort(Comparator
            .comparing(ScheduleItemDto::getDate)
            .thenComparingInt(ScheduleItemDto::getSlotIndex));
        return schedule;
    }
    
    /*@Override
    @Transactional
    public List<ScheduleItemDto> getWeeklySchedule(TA ta, LocalDateTime weekStart) {
        List<ScheduleItemDto> schedule = new ArrayList<>();
        LocalDate weekStartDate = weekStart.toLocalDate();

        // 1) First handle LESSONS specially
        List<Lesson> lessons = lessonRepo.findBySection_AssignedTas_Id(ta.getId());
        for (Lesson lesson : lessons) {
            // compute the actual LocalDate for this lesson in the given week
            java.time.DayOfWeek javaDayOfWeek =
                java.time.DayOfWeek.valueOf(lesson.getDay().name());
            LocalDate lessonDate =
                weekStartDate.with(TemporalAdjusters.nextOrSame(javaDayOfWeek));

            // then split by time exactly as before, but force dto.date = lessonDate
            Event dur = lesson.getDuration();
            int startTotal = dur.getStart().getHour() * 60 + dur.getStart().getMinute();
            int endTotal   = dur.getFinish().getHour() * 60 + dur.getFinish().getMinute();
            int totalMins  = endTotal - startTotal;
            if (totalMins <= 0) continue;

            int segments = (int)Math.ceil(totalMins / 50.0);
            for (int i = 0; i < segments; i++) {
                int segStart = startTotal + i * 50;
                int slotIdx  = 1 + ((segStart - (8*60 + 30)) / 50);

                ScheduleItemDto dto = new ScheduleItemDto();
                dto.setDate(lessonDate);
                dto.setSlotIndex(slotIdx);
                dto.setType(ScheduleItemType.LESSON);
                dto.setReferenceId(lesson.getLessonId());
                if (i == 0) {
                    dto.setClassroom(lesson.getLessonRoom().getClassroomId());
                    dto.setCode(lesson.getSection().getSectionCode());
                }
                schedule.add(dto);
            }
        }

        // 2) Then TASKS (use embedded Date’s year/month/day)
        for (TaTask tt : taTaskRepo.findAllByTaId(ta.getId())) {
            Event dur = tt.getTask().getDuration();
            addSegments(schedule, dur, ScheduleItemType.TASK,
                tt.getTask().getRoom().getClassroomId(),
                tt.getTask().getSection().getSectionCode(),
                (long)tt.getTask().getTaskId());
        }

        // 3) And PROCOTORINGS the same way
        for (ExamRoom er : ta.getExamRooms()) {
            Event dur = er.getExam().getDuration();
            String roomId = er.getExamRoom()!=null
                ? er.getExamRoom().getClassroomId() : "";
            addSegments(schedule, dur, ScheduleItemType.PROCTORING,
                roomId,
                er.getExam().getCourseOffering().getCourse().getCourseCode(),
                (long)er.getExamRoomId());
        }

        // 4) Finally sort and return
        schedule.sort(Comparator
            .comparing(ScheduleItemDto::getDate)
            .thenComparingInt(ScheduleItemDto::getSlotIndex));
        return schedule;
    }

    /** helper to split any Event into 50-min slots, using the embedded Event’s date */
    private void addSegments(List<ScheduleItemDto> out,
                            Event dur,
                            ScheduleItemType type,
                            String classroom,
                            String code,
                            Long refId) {
        int startTotal = dur.getStart().getHour() * 60 + dur.getStart().getMinute();
        int endTotal   = dur.getFinish().getHour() * 60 + dur.getFinish().getMinute();
        int totalMins  = endTotal - startTotal;
        if (totalMins <= 0) return;

        int segments = (int)Math.ceil(totalMins / 50.0);
        for (int i = 0; i < segments; i++) {
            int segStart = startTotal + i * 50;
            int slotIdx  = 1 + ((segStart - (8*60 + 30)) / 50);

            ScheduleItemDto dto = new ScheduleItemDto();
            // use the actual date fields from the embedded Event
            dto.setDate(LocalDate.of(
                dur.getStart().getYear(),
                dur.getStart().getMonth(),
                dur.getStart().getDay()
            ));
            dto.setSlotIndex(slotIdx);
            dto.setType(type);
            dto.setReferenceId(refId);
            if (i == 0) {
                dto.setClassroom(classroom);
                dto.setCode(code);
            }
            out.add(dto);
        }
    }


    private int computeSlotIndex(LocalDateTime dt) {
            // minutes since 8:30
            int mins = dt.getHour() * 60 + dt.getMinute() - (8 * 60 + 30);
            return 1 + (mins / 50);
    }
    private String returnCourseCode(String sectionCode){
        String[] parts = sectionCode.split("-");
        return parts[0] +"-"+ parts[1] +"-"+ parts[2];
    }
    
    private Date setDay(Date date, com.example.entity.General.DayOfWeek day){
        switch (day) {
            case MONDAY :  {date.setDay(1);break;}
            case TUESDAY : {date.setDay(2);break;}
            case WEDNESDAY :{date.setDay(3);break;}
            case THURSDAY :{date.setDay(4);break;}
            case FRIDAY :{date.setDay(5);break;}
            case SATURDAY :{date.setDay(6);break;}
            case SUNDAY :{date.setDay(7);break;}
            default:
                throw new AssertionError();
        }
        return date;
    }
}