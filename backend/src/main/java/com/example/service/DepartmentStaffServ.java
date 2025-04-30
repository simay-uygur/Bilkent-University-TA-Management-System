package com.example.service;

import com.example.entity.Actors.DepartmentStaff;
import java.util.List;

public interface DepartmentStaffServ {
    DepartmentStaff createDepartmentStaff(DepartmentStaff staff);
    boolean deleteDepartmentStaff(Long id);
    DepartmentStaff updateDepartmentStaff(Long id, DepartmentStaff staff);
    List<DepartmentStaff> getAllDepartmentStaff();
}