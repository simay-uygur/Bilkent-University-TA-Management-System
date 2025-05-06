package com.example.entity.Schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.example.entity.General.Date;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

/*@Entity
@Table(name = "schedule_table")*/
@Embeddable
@Getter
@Setter
public class Schedule {

    private String weekStart;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "schedule_items", joinColumns = @JoinColumn(name = "schedule_id"))
    private List<ScheduleItem> scheduleItems = new ArrayList<>();

    public Schedule() {}

    public Schedule(String weekStart) {
        this.weekStart = weekStart;
    }

    public void addScheduleItem(ScheduleItem item) {
        this.scheduleItems.add(item);
    }

    public static String computeWeekStart(Date customDate) {
        LocalDate ld = LocalDate.of(customDate.getYear(), customDate.getMonth(), customDate.getDay());
        LocalDate monday = ld.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.toString(); // e.g., 2024-04-15
    }

    @Override
    public String toString() {
        return "Schedule [weekStart=" + weekStart + ", scheduleItems=" + scheduleItems + "]";
    }

    public List<ScheduleItem> findDay(String date) {
        List<ScheduleItem> scheduleItems = new ArrayList<>();
        for (ScheduleItem item : scheduleItems) {
            if (item.getDate().equals(weekStart)) {
                scheduleItems.add(item);
            }
        }
        return scheduleItems;
    }   
}
