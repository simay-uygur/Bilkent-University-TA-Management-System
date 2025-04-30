package com.example.service;

import com.example.entity.Courses.Department;
import java.util.List;

public interface DepartmentServ {

    List<Department> getAllDepartments();
    public boolean createDepartment(Department dep);
    public boolean departmentExists(String name);
}