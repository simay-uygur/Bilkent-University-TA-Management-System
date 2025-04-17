package com.example.entity.Schedule;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Day {
    
    private String dayOfWeek; // e.g., "Monday", "Tuesday", etc.
    private String date; // Date in "yyyy-MM-dd" format
    @ElementCollection(fetch = FetchType.EAGER) // Annotate to indicate it's a collection of embeddable elements
    private List<ScheduleItem> scheduleItems; // List of schedule items for this day
    public Day() {}
    
    public Day(String date) {
        this.date = date;
        this.dayOfWeek = java.time.LocalDate.parse(date).getDayOfWeek().toString(); // Convert date to day of week
    }

    public String toString() {
        return String.format("%s", dayOfWeek);
    }
}
