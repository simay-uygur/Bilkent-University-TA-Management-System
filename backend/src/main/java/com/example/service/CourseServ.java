package com.example.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.dto.CourseDto;


import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
import com.example.entity.Tasks.Task;

public interface CourseServ {
    public boolean addSection(String course_code, Section section);
    //public boolean addTask(String course_code, Task task);
    public boolean courseExists(String courseCode);
    public boolean createCourse(Course course);
    public CourseDto findCourse(String courseCode);
    //public boolean assignTA(Long ta_id, String course_code);
    public boolean updateTask(String course_code,int task_id,Task task);
    public Task getTaskByID(String course_code, int task_id);
    public List<CourseDto> getCourses();
    public Map<String, Object> importCoursesFromExcel(MultipartFile file) throws IOException;
    public List<CourseDto> getCoursesByDepartment(String deptName);
    public Boolean deleteCourse(String course_code);

}
