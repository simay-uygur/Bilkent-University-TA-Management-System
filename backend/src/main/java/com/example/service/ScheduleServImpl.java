package com.example.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Lesson;
import com.example.entity.Courses.Section;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;
import com.example.entity.Schedule.ScheduleItemType;
import com.example.entity.Tasks.TA_Task;
import com.example.entity.Tasks.Task;
import com.example.exception.taExc.TaNotFoundExc;
import com.example.repo.TARepo;
import com.example.repo.TA_TaskRepo;

import jakarta.persistence.Embeddable;

@Embeddable
@Service
public class ScheduleServImpl implements ScheduleServ {
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
    @Autowired
    private TARepo taRepo;

    @Autowired
    private TA_TaskRepo taTaskRepo;

    @Override
    public LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    // Example method to build the weekly schedule for a TA.
    @Override
    public Schedule getWeeklyScheduleForTA(TA ta, Date anyCustomDate) {
        // Compute the week start (Monday) using the utility method in Schedule
        String weekStartStr = Schedule.computeWeekStart(anyCustomDate);
        Schedule schedule = new Schedule(weekStartStr);

        // Retrieve tasks and daily works (this should be done via repository/service calls).
        List<Task> tasks = fetchTasksForTA(ta.getId(), weekStartStr);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Process each Task from the TA's task lists.
        for (Task task : tasks) {
            // Obtain the Event from the task, which contains the start Date.
            Event event = task.getDuration();

            // Convert the custom Date to a LocalDate in order to build the day key.
            LocalDate startDate = LocalDate.of(
                    event.getStart().getYear(),
                    event.getStart().getMonth(),
                    event.getStart().getDay()
            );

            String key = startDate.format(dtf);// in format "yyyy-MM-dd"
            String title = task.getTask_type().toString() ;
            ScheduleItem item = new ScheduleItem(title, event, ScheduleItemType.TASK, task.getTask_id(),key);
            if (!schedule.getScheduleItems().contains(item)) {
                schedule.addScheduleItem(item);
            }
        }

        for (Section sec : ta.getTas_own_lessons()) {
            // Obtain the Event from the lesson, which contains the start Date.
            for (Lesson lesson : sec.getLessons()){
                Event event = lesson.getDuration();
                LocalDate startDate = LocalDate.of(
                    event.getStart().getYear(),
                    event.getStart().getMonth(),
                    event.getStart().getDay()
                );
                String key = startDate.format(dtf);// in format "yyyy-MM-dd"
                String title = sec.getSection_code();
                ScheduleItem item = new ScheduleItem(title, event, ScheduleItemType.LESSON, sec.getSection_id(),key);
                if (!schedule.getScheduleItems().contains(item)) {
                    schedule.addScheduleItem(item);
                } 
            }
        }

        return schedule;
    }

    

    // Stub methods to represent data fetching. Replace these with actual repository calls.
    private List<Task> fetchTasksForTA(Long taId, String startDate) {
        //List<TA_Task> ta_tasks = taTaskRepo.findAllPendingTasksByTaId(taId);
        List<TA_Task> ta_tasks = taTaskRepo.findAllByTaId(taId);
        List<Task> tasks_list = new ArrayList<>();
        for (TA_Task ta_task : ta_tasks) {
            Task task = ta_task.getTask();
            if (task != null && task.getStart_date().compareTo(startDate) >= 0) {
                tasks_list.add(task);
            }
        }
        // Combine both lists into a single list of tasks.
        return tasks_list;
    }

    private List<Lesson> fetchLessonsForTA(TA ta) {
        // Retrieve daily duties assigned to the TA.
        return List.of();
    }

    private List<Task> fetchTasksOnDateForTA(Long taId, String date) {
        Optional<TA> ta = taRepo.findById(taId) ;
        if (ta.isEmpty()) {
            throw new TaNotFoundExc(taId);
        }

        TA ta_obj = ta.get() ;
        List<Task> tasks_list = new ArrayList<>();
        for (TA_Task ta_task : ta_obj.getTa_tasks()) {
            Task task = ta_task.getTask();
            if (task != null && task.getStart_date().equals(date)) {
                tasks_list.add(task);
            }
        }
        return tasks_list;
    }

    @Override
    public List<ScheduleItem> getDaySchedule(TA ta, String date) {
        // Fetch the schedule for the specified day.
        List<ScheduleItem> scheduleItems = new ArrayList<>();
        List<Task> tasks = fetchTasksOnDateForTA(ta.getId(), date);
        for(Task task : tasks) {
            Event event = task.getDuration();
            ScheduleItem item = new ScheduleItem(task.getTask_type().toString() + " Task", event, ScheduleItemType.TASK, task.getTask_id(), date);
            scheduleItems.add(item);
        }
        return scheduleItems;
    }
}
