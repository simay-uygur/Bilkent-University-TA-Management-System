package com.example.mapper;

import com.example.dto.SectionDto;
import com.example.dto.LessonDto;
import com.example.dto.InstructorDto;
import com.example.entity.Courses.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        dto.setLessons(section.getLessons().stream()
                .map(lessonMapper::toDto)
                .collect(Collectors.toList()));
        dto.setInstructor(instructorMapper.toDto(section.getInstructor())); 

        dto.setTas(section.getRegisteredTas().stream().map(taMapper::toDto).collect(Collectors.toList()));
        dto.setStudents(section.getRegisteredStudents().stream().map(studentMapper::toDto).collect(Collectors.toList()));

        return dto;
    }

    /** DTO → Entity */
    public Section toEntity(SectionDto dto) {
        if (dto == null) return null;
        Section s = new Section();
        s.setSectionId(dto.getSectionId());
        s.setSectionCode(dto.getSectionCode());
        // wiring s.setOffering(...), s.setInstructor(...) and s.setLessons(...)
        // should happen in your service before saving
        return s;
    }
}