package com.example.entity.Schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.TA;
import com.example.entity.General.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "schedule_table")
@Getter
@Setter
public class Schedule {

    @OneToOne(mappedBy = "schedule")
    // This is the TA that owns this schedule
    private TA ta_owner;

    @Id
    @Column(name = "schedule_id", unique = true, nullable = false)
    private int id; // Unique identifier for the schedule

    // Week always starts from Monday
    private String weekStart;

    // List of days in the schedule
    @Embedded
    @Column(name = "daily_schedule")
    private List<Day> dailySchedule;

    public Schedule(String weekStart) {
        this.weekStart = weekStart;
        this.dailySchedule = new ArrayList<>();
        // Initialize 7 days (Monday to Sunday)
        LocalDate monday = LocalDate.parse(weekStart);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            dailySchedule.add(new Day(day.format(formatter)));
        }
    }

    public void addScheduleItem(String dateKey, ScheduleItem item) {
        Day day = new Day(dateKey);
        if (dailySchedule.contains(day)) {
            dailySchedule.get(indexOf(day)).getScheduleItems().add(item);
        } else {
            List<ScheduleItem> items = new ArrayList<>();
            items.add(item);
            day.setScheduleItems(items);
            dailySchedule.add(day);
        }
    }

    private int indexOf(Day day) {
        for (int i = 0; i < dailySchedule.size(); i++) {
            if (dailySchedule.get(i).getDate().equals(day.getDate())) {
                return i;
            }
        }
        return -1; // Not found
    }

    @Override
    public String toString() {
        return "Schedule [weekStart=" + weekStart + ", dailySchedule=" + dailySchedule + "]";
    }

    public static String computeWeekStart(Date customDate) {
        LocalDate ld = LocalDate.of(customDate.getYear(), customDate.getMonth(), customDate.getDay());
        LocalDate monday = ld.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.toString(); // Format: year-month-day (e.g., 2000-01-01)
    }
}