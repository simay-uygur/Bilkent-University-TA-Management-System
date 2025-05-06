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

    public boolean isOngoing() {
        Date current = new Date().currenDate();
        return (current.isAfter(start) || current.isBefore(start)) && current.isBefore(finish);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event event = (Event) obj;
        return this.start.equals(event.start) && this.finish.equals(event.finish);
    }

    //not checked
    /*public boolean has(Event dur){
        return ((dur.getStart().equals(start) && dur.getFinish().equals(finish)) || 
                ((dur.getStart().getHour() >= start.getHour() && 
                  dur.getStart().getMinute() >= start.getMinute()) && 
                  (dur.getStart().getHour() <= finish.getHour() &&
                  dur.getStart().getMinute() >= finish.getMinute())) ||
                ((dur.getFinish().getHour() >= start.getHour() &&
                  dur.getFinish().getMinute() >= start.getMinute()) && 
                  (dur.getFinish().getHour() <= finish.getHour() &&
                   dur.getFinish().getMinute() <= finish.getMinute())));
    }*/

    public boolean has(Event other){
        if ( this.finish.isBefore(other.getStart())
        || this.start .isAfter(other.getFinish()) ) {
            return false;
        }
        // otherwise we must overlap (start inside, end inside, fully containing, etc.)
        return true;
    }

    @Override
    public String toString(){
        return "Start at: " + start.toString() + "\nFinish at: " + finish.toString();
    }

}
