package com.example.dto;

import com.example.entity.General.Event;

public class TaTaskDto {
    private String type;
    private String description;
    private Event duration;
    private String status;
    private int workload;

    public TaTaskDto() {
    }

    public TaTaskDto(String type, String description, Event duration, String status, int workload) {
        this.type = type;
        this.description = description;
        this.duration = duration;
        this.status = status;
        this.workload = workload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Event getDuration() {
        return duration;
    }

    public void setDuration(Event duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }
}
