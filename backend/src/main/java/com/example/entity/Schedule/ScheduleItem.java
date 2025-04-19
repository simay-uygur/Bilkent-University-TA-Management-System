package com.example.entity.Schedule;
import com.example.entity.General.Event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class ScheduleItem {

    // A brief title or description for the schedule item.
    @Column(name = "title", unique = false, updatable = true, nullable = false)
    private String title;
    
    // The date for the schedule item, in "yyyy-MM-dd" format.
    @Column(name = "date", unique = false, updatable = true, nullable = false)
    private String date;

    // duration
    @Column(name = "duration", unique = false, updatable = true, nullable = false)
    private Event duration;
    
    // Task or a DailyWork.
    @Enumerated(EnumType.STRING)
    @Column(name = "type", unique = false, updatable = true, nullable = false)
    private ScheduleItemType type;
    
    // reference ID to the original Task or DailyWork class
    @Column(name = "reference_id", unique = false, updatable = true, nullable = false)
    private int referenceId;

    public ScheduleItem(String title, Event duration, ScheduleItemType type, int referenceId, String date) {
        this.date = date;
        this.title = title;
        this.duration = duration;
        this.type = type;
        this.referenceId = referenceId;
    }
    
    @Override
    public String toString() {
        return "ScheduleItem [title=" + title + ", event=" + duration + ", type=" + type + ", referenceId=" + referenceId + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleItem that = (ScheduleItem) o;
        return referenceId == that.referenceId && title.equals(that.title) && duration.equals(that.duration) && type == that.type;
    }
}
