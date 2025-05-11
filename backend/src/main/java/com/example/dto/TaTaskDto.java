package com.example.dto;

import com.example.entity.General.Event;

public class TaTaskDto {
    private int taskId;
    private Long taId;
    private String type;
    private String description;
    private Event duration;
    private String status;
    private int workload;

    public TaTaskDto() {
    }

    public TaTaskDto(int taskId, Long taId, String type, String description, Event duration, String status, int workload) {
        this.taskId = taskId;
        this.taId = taId;
        this.type = type;
        this.description = description;
        this.duration = duration;
        this.status = status;
        this.workload = workload;
    }

    public int getTaskId(){
        return taskId;
    }

    public void setTaskId(int taskId){
        this.taskId = taskId;
    }

    public Long getTaId(){
        return taId;
    }

    public void setTaId(Long taId){
        this.taId = taId;
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
