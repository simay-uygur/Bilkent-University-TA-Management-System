package com.example.service;

import java.util.List;

import com.example.entity.Courses.Course;
import com.example.entity.Courses.Course_DTO;
import com.example.entity.Courses.Section;
import com.example.entity.Tasks.Task;

public interface CourseServ {
    public boolean addSection(String course_code, Section section);
    public boolean addTask(String course_code, Task task);
    public boolean courseExists(String course_code);
    public boolean createCourse(Course course);
    public Course_DTO findCourse(String course_code);
    public boolean assignTA(Long ta_id, String course_code);
    public boolean updateTask(String course_code,int task_id,Task task);
    public Task getTaskByID(String course_code, int task_id);
    public List<Course_DTO> getTasks();
}
