package com.example.mapper;

import com.example.dto.LessonDto;
import com.example.dto.SectionDto;
import com.example.entity.Courses.Section;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SectionMapper {

    private final LessonMapper lessonMapper;
    private final InstructorMapper instructorMapper;

    public SectionMapper(LessonMapper lessonMapper,
                         InstructorMapper instructorMapper) {
        this.lessonMapper = lessonMapper;
        this.instructorMapper = instructorMapper;
    }

    public SectionDto toDto(Section section) {
        List<LessonDto> lessons = section.getLessons().stream()
                .map(lessonMapper::toDto)    // ← instance method reference!
                .collect(Collectors.toList());

        return new SectionDto(
                section.getSectionId(),
                section.getSectionCode(),
                lessons,
                instructorMapper.toDto(section.getInstructor())
        );
    }

    public Section toEntity(SectionDto dto) {
        Section s = new Section();
        s.setSectionId(dto.getSectionId());
        s.setSectionCode(dto.getSectionCode());
        // leave instructor & lessons wiring to your service layer
        return s;
    }
}
