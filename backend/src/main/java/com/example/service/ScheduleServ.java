package com.example.service;

import java.time.LocalDate;

import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Schedule.Schedule;


public interface ScheduleServ {
    public LocalDate getWeekStart(LocalDate date);
    public Schedule getWeeklyScheduleForTA(TA ta, Date anyCustomDate);
    // Stub methods to represent data fetching. Replace these with actual repository calls.
}
