package com.example.entity.Tasks;

import com.example.entity.Actors.TA;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TA_Tasks")
public class TA_Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)// auto id generation
    private int id; // Composite key

    // Many-to-one relationship with Task
    @ManyToOne
    @JoinColumn(name = "task_id", insertable = true, updatable = false)
    private Task task;

    // Many-to-one relationship with TA
    //@JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ta_id", insertable = true, updatable = false)
    private TA ta_owner;

    @Column(name = "access_type", unique = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TaskAccessType type;

    // Parameterized constructor
    public TA_Task(Task task, TA ta_owner, TaskAccessType type) {
        this.ta_owner = ta_owner;
        this.task = task;
        this.type = type;
    }

    // Default constructor
    public TA_Task() {}
}