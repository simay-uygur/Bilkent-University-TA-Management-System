package com.example.entity.Courses;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "department")
public class Department {
    
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "dep_id", unique = true)
    private int dep_id;

    @Column(name = "dep_name", unique= true)
    private String dep_name;
}
