package com.example.entity.Curriculum;

import com.example.entity.General.Event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Embeddable
public class DailyDuties {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE) // auto id generation
    @Column(name = "task_id", unique = true, updatable = false, nullable = false)
    private int _id;

    @Embedded
    @Column(name = "duration", unique = false, updatable = true, nullable = false)
    private Event duration;
}
