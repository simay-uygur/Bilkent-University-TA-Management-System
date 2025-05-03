
package com.example.mapper;

import com.example.dto.TaDto;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
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

        // “Courses” → the courseCode of each CourseOffering this TA is assigned to
        List<String> courses = ta.getOfferingsAsHelper().stream()
                .map(offering -> offering.getCourse().getCourseCode())
                .distinct()
                .collect(Collectors.toList());
        dto.setCourses(courses);

        // “Lessons” → the sectionCode of each Section this TA is registered in as a student
        List<String> lessons = ta.getSectionsAsStudent().stream()
                .map(Section::getSectionCode)
                .distinct()
                .collect(Collectors.toList());
        dto.setLessons(lessons);

        return dto;
    }

//    public TaDto toDto(TA ta) {
//        TaDto dto = new TaDto();
//        dto.setId(ta.getId());
//        dto.setName(ta.getName());
//        dto.setSurname(ta.getSurname());
//        dto.setAcademicLevel(ta.getAcademicLevel().name());
//        dto.setTotalWorkload(ta.getTotalWorkload());
//        dto.setIsActive(ta.getIsActive());
//        dto.setIsGraduated(ta.getIsGraduated());
//        dto.setDepartment(ta.getDepartment());
//
//        List<String> courses = ta.getCourses().stream()
//                .map(Course::getCourseCode)
//                .collect(Collectors.toList());
//        dto.setCourses(courses);
//
//        // section
////        List<String> lessons = ta.getTas_own_lessons().stream()
////                .map(Section::getSection_id)
////                .collect(Collectors.toList());
////        dto.setLessons(lessons);
//
//        return dto;
//    }
}
