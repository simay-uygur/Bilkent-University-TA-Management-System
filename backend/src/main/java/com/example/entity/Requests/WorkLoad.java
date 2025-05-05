package com.example.entity.Requests;

import com.example.entity.Tasks.Task;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "workload_requests")
@Entity
public class WorkLoad extends Request{
    @ManyToOne
    @JoinColumn(name = "task_id", referencedColumnName = "task_id")
    private Task task;
}
