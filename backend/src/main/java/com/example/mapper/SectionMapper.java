package com.example.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.dto.SectionDto;
import com.example.entity.Courses.Section;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SectionMapper {

    private final LessonMapper lessonMapper;
    private final InstructorMapper instructorMapper;
    private final TaMapper taMapper;
    private final StudentMapper studentMapper;

    /** Entity → DTO */
    public SectionDto toDto(Section section) {
        if (section == null) return null;

        SectionDto dto = new SectionDto();
        dto.setSectionId(section.getSectionId());
        dto.setSectionCode(section.getSectionCode());
        dto.setLessons(
            section.getLessons().stream()
                   .map(lessonMapper::toDto)
                   .collect(Collectors.toList())
        );
        dto.setInstructor(
            instructorMapper.toDto(section.getInstructor())
        );
        dto.setTas(
            section.getRegisteredTas().stream()
                   .map(taMapper::toDto)
                   .collect(Collectors.toList())
        );
        dto.setStudents(
            section.getRegisteredStudents().stream()
                   .map(studentMapper::toDto)
                   .collect(Collectors.toList())
        );

        // Yeni: CourseOffering'in coordinator ID'sini set et
        if (section.getOffering() != null
         && section.getOffering().getCoordinator() != null) {
            dto.setCoordinatorId(
                section.getOffering()
                       .getCoordinator()
                       .getId()
            );
        }

        return dto;
    }

    /** DTO → Entity */
    public Section toEntity(SectionDto dto) {
        if (dto == null) return null;
        Section s = new Section();
        s.setSectionId(dto.getSectionId());
        s.setSectionCode(dto.getSectionCode());

        if (dto.getLessons() != null) {
            s.setLessons(
                dto.getLessons().stream()
                   .map(lessonMapper::toEntity)
                   .collect(Collectors.toList())
            );
        }
        // instructor, tas, students ve offering ilişkisi servis katmanında setlenecek

        return s;
    }
}
