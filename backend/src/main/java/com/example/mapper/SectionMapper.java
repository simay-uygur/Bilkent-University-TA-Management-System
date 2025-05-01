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

    /** Entity → DTO */
    public SectionDto toDto(Section section) {
        if (section == null) return null;

        List<LessonDto> lessons = section.getLessons().stream()
                .map(lessonMapper::toDto)
                .collect(Collectors.toList());

        InstructorDto instr = instructorMapper.toDto(section.getInstructor());

        return new SectionDto(
                section.getSectionId(),
                section.getSectionCode(),
                lessons,
                instr
        );
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