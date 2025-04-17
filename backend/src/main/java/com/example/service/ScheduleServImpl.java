package com.example.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Actors.TA;
import com.example.entity.Curriculum.Lesson;
import com.example.entity.General.Date;
import com.example.entity.General.Event;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;
import com.example.entity.Schedule.ScheduleItemType;
import com.example.entity.Tasks.TA_Task;
import com.example.entity.Tasks.Task;
import com.example.repo.TARepo;
import com.example.repo.TA_TaskRepo;

import jakarta.persistence.Embeddable;
import lombok.RequiredArgsConstructor;

@Embeddable
@Service
@RequiredArgsConstructor
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
        // In this example, we use stub methods.
        List<Task> tasks = fetchTasksForTA(ta.getId());
        List<Lesson> tas_lessons = fetchLessonsForTA(ta);

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
            String key = startDate.format(dtf);
            String title = task.getTask_type().toString() + " Task";
            ScheduleItem item = new ScheduleItem(title, event, ScheduleItemType.TASK, task.getTask_id());
            schedule.addScheduleItem(key, item);
        }

        // Process each Lesson entry similarly.
        for (Lesson lesson : tas_lessons) {
            Event event = lesson.getDuration();
            LocalDate startDate = LocalDate.of(
                    event.getStart().getYear(), 
                    event.getStart().getMonth(), 
                    event.getStart().getDay()
            );
            String key = startDate.format(dtf);
            String title = lesson.getDuty_type().toString() + " Duty";
            // Reference id can be 0 (or use a proper id if available in Lesson).
            ScheduleItem item = new ScheduleItem(title, event, ScheduleItemType.DAILY_WORK, lesson.getDuty_id());
            schedule.addScheduleItem(key, item);
        }

        return schedule;
    }

    // Stub methods to represent data fetching. Replace these with actual repository calls.
    private List<Task> fetchTasksForTA(Long taId) {
        List<TA_Task> ta_tasks = taTaskRepo.findAllPendingTasksByTaId(taId);
        List<Task> tasks_list = new ArrayList<>();
        for (TA_Task ta_task : ta_tasks) {
            Task task = ta_task.getTask();
            if (task != null) {
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
}
