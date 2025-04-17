package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.entity.Courses.Course;
import com.example.service.CourseServ;

public class Course_controller {
    @Autowired
    private CourseServ courseServ; // this is used to check if the course exists in the database




    private void checkPrerequisites(Course course) {
        if (course.getPrereq_list() != null) {
            for (String prereq : course.getPrereq_list()){
                int code = course.code_to_id(prereq); // converts course code to id
                if (courseServ.courseExists(code)) {
                    throw new IllegalArgumentException("Prerequisite course not found: " + prereq);
                }
            }
        }
    }
}
