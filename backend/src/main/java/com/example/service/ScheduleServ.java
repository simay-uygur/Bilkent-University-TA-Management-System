package com.example.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.entity.Actors.TA;
import com.example.entity.Schedule.ScheduleItemDto;


public interface ScheduleServ {
    public LocalDate getWeekStart(LocalDate date);
    /*public Schedule getWeeklyScheduleForTA(TA ta, Date anyCustomDate);
    public List<ScheduleItem> getDaySchedule(TA ta, String date);*/
    public List<ScheduleItemDto> getWeeklySchedule(Long taId, LocalDateTime weekStart);
    // Stub methods to represent data fetching. Replace these with actual repository calls.
}
