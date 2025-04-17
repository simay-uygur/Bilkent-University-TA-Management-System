package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.repo.CourseRepo;

public class CourseServImpl implements CourseServ{

    @Autowired
    private CourseRepo courseRepo; // this is used to check if the course exists in the database

    @Override
    public boolean addSection(int courseId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addTask(int courseId, int taskId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean courseExists(int courseId) {
        return courseRepo.existsById(courseId);
    }
}
