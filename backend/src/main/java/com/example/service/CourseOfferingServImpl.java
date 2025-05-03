// com/example/service/CourseOfferingServiceImpl.java
package com.example.service;

import com.example.dto.CourseOfferingDto;
import com.example.entity.Courses.CourseOffering;
import com.example.mapper.CourseMapper;
import com.example.mapper.CourseOfferingMapper;
import com.example.repo.CourseOfferingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseOfferingServImpl implements CourseOfferingServ {
    private final CourseOfferingRepo repo;
    private final SemesterServ semesterServ;
    private final CourseOfferingMapper courseMapper;

    @Override
    public List<CourseOfferingDto> getOfferingsByDepartment(String deptName){
        List<CourseOffering> offerings = repo.findByCourseDepartmentName(deptName)
                .orElseThrow(() -> new IllegalArgumentException("No offerings found for department: " + deptName));
        
                return offerings.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseOffering create(CourseOffering offering) {
        Long courseId = (long) offering.getCourse().getCourseId();
        Long semesterId = offering.getSemester().getId();

        Optional<CourseOffering> existing = repo.findByCourse_CourseIdAndSemester_Id(courseId, semesterId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Offering already exists for this course and semester.");
        }

        offering.setSemester(semesterServ.getById(semesterId));

        return repo.save(offering);
    }
    @Override
    public CourseOffering update(Long id, CourseOffering offering) {
        CourseOffering existing = getById(id);
        existing.setSemester(offering.getSemester());
        existing.setCourse(offering.getCourse());
        // you could update other fields here
        return repo.save(existing);
    }

    @Override
    public CourseOffering getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + id));
    }

    @Override
    public List<CourseOffering> getAll() {
        return repo.findAll();
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}