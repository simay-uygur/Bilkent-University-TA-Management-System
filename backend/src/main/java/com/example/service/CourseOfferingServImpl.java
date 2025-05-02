// com/example/service/CourseOfferingServiceImpl.java
package com.example.service;

import com.example.entity.Courses.CourseOffering;
import com.example.repo.CourseOfferingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseOfferingServImpl implements CourseOfferingServ {
    private final CourseOfferingRepo repo;

    @Override
    public CourseOffering create(CourseOffering offering) {
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