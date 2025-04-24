package com.example.entity.General;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Date {

    @Column(nullable = false)
    private int day;
    @Column(nullable = false)
    private int month;
    @Column(nullable = false)
    private int year;
    
    
    @Column(nullable = false)
    private int hour;

    @Column(nullable = false)
    private int minute;

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
    
    public boolean isBefore(Date other) { // other - 11:00, this - 11:00
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

    public Date(int day, int month, int year, int hour, int minute) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
    }

    public Date currenDate() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Date currentDate = new Date();
        currentDate.setDay(now.getDayOfMonth());
        currentDate.setMonth(now.getMonthValue());
        currentDate.setYear(now.getYear());
        currentDate.setHour(now.getHour());
        currentDate.setMinute(now.getMinute());
        return currentDate;
    }
}
