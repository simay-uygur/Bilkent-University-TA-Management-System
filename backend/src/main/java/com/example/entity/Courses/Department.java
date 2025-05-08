package com.example.entity.Courses;

import java.util.List;

import com.example.entity.General.Faculty;
import com.example.entity.Requests.ProctorTaInDepartment;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_code")
    private Faculty faculty;

    @OneToMany(
        mappedBy = "department",
        fetch = FetchType.LAZY,
        orphanRemoval = true,
        cascade= CascadeType.ALL
    )
    private List<Course> courses;

    @OneToMany(
        mappedBy= "receiver",
        fetch = FetchType.LAZY,
        orphanRemoval = true,
        cascade= CascadeType.ALL)
    private List<ProctorTaInDepartment> receivedFromInstructorRequests;

    
}
