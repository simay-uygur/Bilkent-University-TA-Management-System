package com.example.entity.Actors;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Courses.Course;
import com.example.entity.Courses.Department;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "instructor_table")
@DynamicUpdate // for needed rows only in sql -
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor //this was missing
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Instructor extends User{

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_instructor",
        joinColumns = @JoinColumn(name = "instructor_id"),
        inverseJoinColumns = @JoinColumn(name = "courseId")
    )
    private List<Course> courses = new ArrayList<>();

    @Column(name = "is_active", updatable = false,  nullable = false)  //added new
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

}
