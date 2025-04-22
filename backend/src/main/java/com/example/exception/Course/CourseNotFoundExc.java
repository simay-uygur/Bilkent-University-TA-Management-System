package com.example.exception.Course;

public class CourseNotFoundExc extends RuntimeException{
    public CourseNotFoundExc(String course_id){
        super("Course with code " + course_id + " not found!");
    }
}
