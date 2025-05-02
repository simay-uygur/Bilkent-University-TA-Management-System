package com.example.mapper;

import com.example.dto.CourseOfferingDto;
import com.example.entity.Courses.CourseOffering;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseOfferingMapper {

    private final CourseMapper courseMapper;
    private final SemesterMapper semesterMapper;

    public CourseOfferingDto toDto(CourseOffering offering) {
        if (offering == null) return null;

        CourseOfferingDto dto = new CourseOfferingDto();
        dto.setId(offering.getId());
        dto.setCourse(courseMapper.toDto(offering.getCourse()));
        dto.setSemester(semesterMapper.toDto(offering.getSemester()));
        return dto;
    }

    public CourseOffering toEntity(CourseOfferingDto dto) {
        if (dto == null) return null;

        CourseOffering offering = new CourseOffering();
        offering.setId(dto.getId());
        offering.setCourse(courseMapper.toEntity(dto.getCourse()));
        offering.setSemester(semesterMapper.toEntity(dto.getSemester()));
        return offering;
    }
}