package com.example.mapper;

import com.example.dto.InstructorDto;
import com.example.entity.Actors.Instructor;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InstructorMapper {

//    public InstructorDto toDto(Instructor inst) {
//        if (inst == null) return null;
//        InstructorDto dto = new InstructorDto();
//        dto.setId(inst.getId());
//        dto.setName(inst.getName());
//        dto.setSurname(inst.getSurname());
//        dto.setDepartmentName(
//                inst.getDepartment() != null
//                        ? inst.getDepartment().getName()
//                        : null
//        );
//        List<String> codes = inst.getCourses().stream()
//                .map(Course::getCourseCode)
//                .collect(Collectors.toList());
//        dto.setCourseCodes(codes);
//        return dto;
//    }
//
//    public Instructor toEntity(InstructorDto dto) {
//        if (dto == null) return null;
//        Instructor inst = new Instructor();
//        inst.setName(dto.getName());
//        inst.setSurname(dto.getSurname());
//        inst.setWebmail(dto.getWebmail());
//        // wiring department & courses must happen in service
//        return inst;
//    }

    public InstructorDto toDto(Instructor inst) {
        if (inst == null) return null;

        InstructorDto dto = new InstructorDto();
        dto.setId(inst.getId());
        dto.setName(inst.getName());
        dto.setSurname(inst.getSurname());
        dto.setWebmail(inst.getWebmail());
        dto.setDepartmentName(
                inst.getDepartment() != null
                        ? inst.getDepartment().getName()
                        : null
        );

        // derive all courseCodes via the sections→offering→course link
        List<String> codes = inst.getSections().stream()
                .map(Section::getOffering)
                .map(o -> o.getCourse().getCourseCode())
                .distinct()
                .collect(Collectors.toList());
        dto.setSections(codes);

        return dto;
    }
    public List<InstructorDto> toDtoList(List<Instructor> instructors) {
        for (Instructor instructor : instructors) {
            InstructorDto dto = toDto(instructor);
            dto.setId(instructor.getId());
            dto.setName(instructor.getName());
            dto.setSurname(instructor.getSurname());
            //dto.setIsActive(instructor.getIsActive()); -  active is not a field in dto
        }
        return instructors.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    public Instructor toEntity(InstructorDto dto) {
        if (dto == null) return null;
        Instructor inst = new Instructor();
        inst.setName(dto.getName());
        inst.setSurname(dto.getSurname());
        inst.setWebmail(dto.getWebmail());
        // department, sections etc. should be wired in the service layer
        return inst;
    }
}