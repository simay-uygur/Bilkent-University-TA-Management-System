package com.example.demo1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
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

    public String toString() {
        return String.format("%02d/%02d/%04d %02d:%02d", day, month, year, hour, minute);
    }
    
    public boolean isBefore(Date other) {
        if (this.year != other.year) return this.year < other.year;
        if (this.month != other.month) return this.month < other.month;
        if (this.day != other.day) return this.day < other.day;
        if (this.hour != other.hour) return this.hour < other.hour;
        return this.minute < other.minute;
    }
    
    public boolean isAfter(Date other) {
        return other.isBefore(this);
    }
}
