package com.example.entity.Actors;

import com.example.entity.Courses.Department;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "department_staff")
@Getter
@Setter
public class DepartmentStaff extends User {

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}