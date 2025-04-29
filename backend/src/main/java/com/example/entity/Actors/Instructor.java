package com.example.entity.Actors;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Courses.Course;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "instructor_table")
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Instructor extends User{
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_instructor",
        joinColumns = @JoinColumn(name = "instructor_id"),
        inverseJoinColumns = @JoinColumn(name = "courseId")
    )
    private List<Course> courses = new ArrayList<>();
}
