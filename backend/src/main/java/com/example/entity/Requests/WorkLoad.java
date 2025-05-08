package com.example.entity.Requests;

import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Tasks.Task;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "workload_requests")
@Entity
public class WorkLoad extends Request{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_ta_id", referencedColumnName = "id")
    private TA sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_instructor_id", referencedColumnName = "id")
    private Instructor receiver;

    @ManyToOne
    @JoinColumn(name = "task_id", referencedColumnName = "task_id")
    private Task task;
}
