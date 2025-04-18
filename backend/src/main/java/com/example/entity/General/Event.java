package com.example.entity.General;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Event {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "year", column = @Column(name = "start_year")),
        @AttributeOverride(name = "month", column = @Column(name = "start_month")),
        @AttributeOverride(name = "day", column = @Column(name = "start_day")),
        @AttributeOverride(name = "hour", column = @Column(name = "start_hour")),
        @AttributeOverride(name = "minute", column = @Column(name = "start_minute"))
    })
    private Date start;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "year", column = @Column(name = "finish_year")),
        @AttributeOverride(name = "month", column = @Column(name = "finish_month")),
        @AttributeOverride(name = "day", column = @Column(name = "finish_day")),
        @AttributeOverride(name = "hour", column = @Column(name = "finish_hour")),
        @AttributeOverride(name = "minute", column = @Column(name = "finish_minute"))
    })
    private Date finish;

    // Utility method to check if event is ongoing
    public boolean isOngoing() {
        Date current = new Date().currenDate();
        return (current.isAfter(start) || current.isBefore(start)) && current.isBefore(finish);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event event = (Event) obj;
        return start.equals(event.start) && finish.equals(event.finish);
    }
}
