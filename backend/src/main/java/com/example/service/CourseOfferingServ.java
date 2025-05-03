// com/example/service/CourseOfferingService.java
package com.example.service;

import com.example.dto.CourseDto;
import com.example.dto.CourseOfferingDto;
import com.example.entity.Courses.CourseOffering;
import java.util.List;

public interface CourseOfferingServ {
    CourseOffering create(CourseOffering offering);
    CourseOffering update(Long id, CourseOffering offering);
    CourseOffering getById(Long id);
    List<CourseOffering> getAll();
    void delete(Long id);
     public List<CourseOfferingDto> getOfferingsByDepartment(String deptName);
}



