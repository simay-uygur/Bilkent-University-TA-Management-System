package com.example.entity.Schedule;
import com.example.entity.General.Event;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter 
public class ScheduleItem {

    // A brief title or description for the schedule item.
    private String title;
    
    // duration
    private Event duration;
    
    // Task or a DailyWork.
    private ScheduleItemType type;
    
    // reference ID to the original Task or DailyWork class
    private int referenceId;

    public ScheduleItem(String title, Event duration, ScheduleItemType type, int referenceId) {
        this.title = title;
        this.duration = duration;
        this.type = type;
        this.referenceId = referenceId;
    }
    
    @Override
    public String toString() {
        return "ScheduleItem [title=" + title + ", event=" + duration + ", type=" + type + ", referenceId=" + referenceId + "]";
    }
}
