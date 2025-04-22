package com.example.exception.Course;

public class NoPrereqCourseFound extends RuntimeException{
    public NoPrereqCourseFound(String course_code){
        super("There is no such course with id " + course_code + " in the prerequisite list");
    }
}
