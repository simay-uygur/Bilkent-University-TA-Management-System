package com.example.entity.General;

import java.util.HashSet;
import java.util.Set;

import com.example.entity.Courses.Course;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
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
    private Long student_id;

    @Column(name = "student_name", unique = false, updatable = true, nullable = false)
    private String student_name;

    @Column(name = "student_surname", unique = false, updatable = true, nullable = false)
    private String student_surname;
    // what is mappedBy?
    // mappedBy is used to specify the owner of the relationship. In this case, the owner is the Course class.
    @ManyToMany(
        mappedBy = "students_list",
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JsonIgnore
    private Set<Course> student_courses = new HashSet<>();
}
