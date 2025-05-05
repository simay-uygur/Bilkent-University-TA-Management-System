// com/example/service/CourseOfferingServiceImpl.java
package com.example.service;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.dto.CourseOfferingDto;
import com.example.entity.Courses.CourseOffering;
import com.example.exception.Course.CourseNotFoundExc;
import com.example.exception.GeneralExc;
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
    public CourseOfferingDto getCourseByCourseCode(String code) {
      CourseOffering off = repo.findByCourseCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + code));

        return courseMapper.toDto(off);
    }
    @Override
    public List<CourseOfferingDto> getCoursesByCourseCode(String code) {
        List<CourseOffering> off = repo.findByCoursesCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + code));

        return off.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }
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

        CourseOffering existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + id));
        existing.setSemester(offering.getSemester());
        existing.setCourse(offering.getCourse());
        // you could update other fields here
        return repo.save(existing);
    }

    @Override
    public CourseOfferingDto getById(Long id) {
        CourseOffering off = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + id));

        return courseMapper.toDto(off);
    }
    /* public List<CourseOfferingDto> getByCourseCode(String code) {
        List<CourseOffering> off = repo.findByCourseCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Offering not found: " + code));

        return off.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    } */

    @Override
    public List<CourseOffering> getAll() {
        return repo.findAll();
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Optional<CourseOffering> getByCourseAndSemester(Long courseId, Long semesterId) {
        return repo.findByCourse_CourseIdAndSemester_Id(courseId, semesterId);
    }

    //this should be written
    @Override
    public boolean assignTA(Long taId, String courseCode) {
        return false;
    }
    //old one - fix needed
//    @Override
//    public boolean assignTA(Long taId, String courseCode) {
//        Course course = repo.findCourseByCourseCode(courseCode)
//                .orElseThrow(() -> new CourseNotFoundExc(courseCode));
//        TA ta = taServ.getTAById(taId);
//        if (ta.getSectionsAsHelper().contains(course)) {
//            throw new GeneralExc("TA " + taId + " already assigned to " + courseCode);
//        }
//        if (ta.get().stream()
//                .anyMatch(sec -> sec.getOffering().getCourse().getCourseCode().equals(courseCode))) {
//            throw new GeneralExc("TA " + taId + " takes this course as a student");
//        }
//        course.getCourseTas().add(ta);
//        courseRepo.save(course);
//        return true;
//    }
//
}