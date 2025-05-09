package com.example.entity.General;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Courses.CourseOffering;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "student")
public class Student {

    @Id
    @Column(name = "student_id", unique = true, updatable = true, nullable = false)
    private Long studentId;

    @Column(name = "student_name", unique = false, updatable = true, nullable = false)
    private String studentName;

    @Column(name = "student_surname", unique = false, updatable = true, nullable = false)
    private String studentSurname;

    @Column(name = "webmail", nullable = true) // newly added - should ,t be nullable or not?
    private String webmail;

    //not sure about nullables
    @Column(name = "academic_status", nullable = true)
    private String academicStatus;

    @Column(name = "department", nullable = true)
    private String department;

    //this will be deleted
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_graduated", nullable = false)
    private Boolean isGraduated = false;

    //student registered courses
    @ManyToMany(mappedBy = "registeredStudents", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CourseOffering> registeredCourseOfferings = new ArrayList<>();
}
