package com.example.entity.Courses;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "departments")
public class Department {

    @Id
    private String name;  // example: "CS", "MATH", "EE"

    @OneToMany(
        mappedBy = "department",
        fetch = FetchType.LAZY,
        orphanRemoval = true,
        cascade= CascadeType.ALL
    )
    private List<Course> courses;
}