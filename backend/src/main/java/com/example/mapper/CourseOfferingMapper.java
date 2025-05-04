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
    private final SectionMapper sectionMapper;
    private final StudentMapper studentMapper;
    private final TaMapper taMapper;
    private final InstructorMapper instructorMapper;

    public CourseOfferingDto toDto(CourseOffering offering) {
        if (offering == null) return null;

        CourseOfferingDto dto = new CourseOfferingDto();
        dto.setId(offering.getId());
        dto.setCourse(courseMapper.toDto(offering.getCourse()));
        dto.setSemester(semesterMapper.toDto(offering.getSemester()));
        dto.setSections(offering.getSections().stream()
                .map(sectionMapper::toDto)
                .toList()
            );


        // dto.setStudents(offering.getStudents().stream()
        //         .map(studentMapper::toDto)
        //         .toList()
        //     );
        // dto.setTas(offering.getTas().stream()
        //         .map(taMapper::toDto)
        //         .toList()
        //     );
       
            
            
        
        //dto.setCoordinator(instructorMapper.toDto(offering.getCoordinator()));
        //dto.setInstructors(instructorMapper.toDtoList(offering.getInstructors()));

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