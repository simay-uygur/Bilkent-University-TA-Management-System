package com.example.entity.General;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
        if (start == null || finish == null) return false;

        Date current = new Date().currenDate();
        //return (current.isAfter(start) || current.isBefore(start)) && current.isBefore(finish);
        return current.isAfter(start) && current.isBefore(finish);
    }

//    public boolean isOngoing() {
//        Date current = new Date().currenDate();
//        return (current.isAfter(start) || current.isBefore(start)) && current.isBefore(finish);
//    }

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

    public boolean hasLesson(int lessonDay, Event event){
        int eventDay = event.getStart().getDay();
        if (eventDay != lessonDay) {
            return false;
        }

        // 2) compute “minutes since midnight” for lesson start/end
        Date ls = this.getStart();
        Date lf = this.getFinish();
        int lessonStart = ls.getHour() * 60 + ls.getMinute();
        int lessonEnd   = lf.getHour() * 60 + lf.getMinute();

        // 3) compute “minutes since midnight” for event start/end
        Date es = event.getStart();
        Date ef = event.getFinish();
        int eventStart = es.getHour() * 60 + es.getMinute();
        int eventEnd   = ef.getHour() * 60 + ef.getMinute();

        // 4) intervals [eventStart,eventEnd) and [lessonStart,lessonEnd) overlap?
        return eventStart < lessonEnd && eventEnd > lessonStart;
    }

    @Override
    public String toString(){
        return "Start at: " + start.toString() + "\nFinish at: " + finish.toString();
    }

}
