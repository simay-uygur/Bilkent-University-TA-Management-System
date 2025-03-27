package com.example.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Section {
    @Id
    private int section_id;

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
        name = "section_tasks",
        joinColumns = @JoinColumn(name = "section_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private Set<Task> section_tasks_list = new HashSet<Task>();
}
