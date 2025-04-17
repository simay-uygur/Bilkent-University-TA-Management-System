package com.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Courses.Course;
@Repository
public interface CourseRepo extends JpaRepository<Course, Integer>{
    
}
