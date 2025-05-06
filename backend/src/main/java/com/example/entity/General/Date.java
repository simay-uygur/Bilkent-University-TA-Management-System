package com.example.entity.General;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Date {

    @Column(nullable = true, updatable = true)
    private Integer day;

    @Column(nullable = true, updatable = true)
    private Integer month;

    @Column(nullable = true, updatable = true)
    private Integer year;

    @Column(nullable = true, updatable = true)
    private Integer hour;

    @Column(nullable = true, updatable = true)
    private Integer minute;

    @Override
    public String toString() {
        return String.format("%04d-%02d-%02d", year, month, day); // yyyy-mm-dd
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Date date = (Date) obj;
        return day == date.day && month == date.month && year == date.year && hour == date.hour && minute == date.minute;
    }
    
    public boolean isBefore(Date other) { // other - 16:48, this - 16:45
        if (this.year != other.year) return this.year < other.year; 
        if (this.month != other.month) return this.month < other.month;
        if (this.day != other.day) return this.day < other.day;
        if (this.hour != other.hour) return this.hour < other.hour;
        return this.minute <= other.minute;
    }
    
    public boolean isAfter(Date other) {
        return other.isBefore(this);
    }

    public Date(){}

    public Date(Integer day, Integer month, Integer year, Integer hour, Integer minute) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
    }

//    public Date(int day, int month, int year, int hour, int minute) {
//        this.day = day;
//        this.month = month;
//        this.year = year;
//        this.hour = hour;
//        this.minute = minute;
//    }

    public Date currenDate() {
        //java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
        Date currentDate = new Date();
        currentDate.setDay(now.getDayOfMonth());
        currentDate.setMonth(now.getMonthValue());
        currentDate.setYear(now.getYear());
        currentDate.setHour(now.getHour());
        currentDate.setMinute(now.getMinute());
        return currentDate;
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(year, month, day);
    }
    
    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(year, month, day, hour, minute);
    }
}
