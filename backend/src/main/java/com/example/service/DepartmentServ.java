package com.example.service;

import com.example.entity.Courses.Department;
import java.util.List;

public interface DepartmentServ {

    List<Department> getAllDepartments();

    boolean createDepartment(Department department);
}