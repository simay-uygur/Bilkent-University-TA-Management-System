package com.example.service;

import java.time.LocalDate;
import java.util.List;

import com.example.entity.Actors.TA;
import com.example.entity.General.Date;
import com.example.entity.Schedule.Schedule;
import com.example.entity.Schedule.ScheduleItem;


public interface ScheduleServ {
    public LocalDate getWeekStart(LocalDate date);
    public Schedule getWeeklyScheduleForTA(TA ta, Date anyCustomDate);
    public List<ScheduleItem> getDaySchedule(TA ta, String date);
    // Stub methods to represent data fetching. Replace these with actual repository calls.
}
