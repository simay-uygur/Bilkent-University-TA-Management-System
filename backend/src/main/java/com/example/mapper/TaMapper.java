
package com.example.mapper;

import com.example.dto.TaDto;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaMapper {

    public TaDto toDto(TA ta) {
        TaDto dto = new TaDto();
        dto.setId(ta.getId());
        dto.setName(ta.getName());
        dto.setSurname(ta.getSurname());
        dto.setAcademicLevel(ta.getAcademicLevel().name());
        dto.setTotalWorkload(ta.getTotalWorkload());
        dto.setIsActive(ta.getIsActive());
        dto.setIsGraduated(ta.getIsGraduated());
        dto.setDepartment(ta.getDepartment());

        List<String> courses = ta.getCourses().stream()
                .map(Course::getCourseCode)
                .collect(Collectors.toList());
        dto.setCourses(courses);

        // section
//        List<String> lessons = ta.getTas_own_lessons().stream()
//                .map(Section::getSection_id)
//                .collect(Collectors.toList());
//        dto.setLessons(lessons);

        return dto;
    }
}
