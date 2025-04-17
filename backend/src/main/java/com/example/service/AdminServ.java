package com.example.service;

import java.util.HashSet;

import com.example.entity.Actors.TA;
import com.example.entity.Actors.User;
import com.example.entity.Courses.Course;
import com.example.entity.Tasks.Task;

public interface AdminServ {
    public HashSet<Task> getAllApprovedTasks();
    public HashSet<Task> getAllUnApprovedTasks();
    public HashSet<Task> getAllTasks();
    public boolean approveTask(Task t);
    public boolean deleteTask(Task t);
    public boolean deleteUser(Task t);
    public boolean deleteAdmin(Task t);
    public boolean deleteTA(Task t);
    public boolean restoreTask(Task t);
    public boolean restoreUser(Task t);
    public boolean updateTask(Task t);
    public HashSet<Task> getAllDeletedTasks();

    public boolean createTA(TA ta);
    public boolean soft_deleteTA(TA ta);
    public boolean strict_deleteTA(TA ta);
    public boolean restoreTA(TA ta);
    public boolean updateTA(TA ta);
    public HashSet<TA> getAllTAs();
    public HashSet<TA> getAllDeletedTAs();
    public HashSet<TA> getAllNotDeletedTAs();

    public HashSet<User> getAllUsers();
    public boolean updateUser(User u);
    public boolean createUser(User u);
    public boolean soft_deleteUser(User u);
    public boolean strict_deleteUser(User u);
    /*public HashSet<Admin> getAllAdmins(); 
      public boolean createAdmin(Admin admin);
      public boolean updateAdmin(Admin admin); Admin admin
      public boolean restoreAdmin(Admin admin);
    */

    public boolean createCourse(Course c);
    public boolean updateCourse(Course c);
}
