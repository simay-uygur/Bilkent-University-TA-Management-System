package com.example.mapper;

import com.example.dto.SemesterDto;
import com.example.entity.General.Semester;
import com.example.entity.General.Term;
import org.springframework.stereotype.Component;

@Component
public class SemesterMapper {

    public SemesterDto toDto(Semester semester) {
        if (semester == null) return null;

        SemesterDto dto = new SemesterDto();
        dto.setId(semester.getId());
        dto.setTerm(String.valueOf(semester.getTerm()));
        dto.setYear(semester.getYear());
        return dto;
    }

    public Semester toEntity(SemesterDto dto) {
        if (dto == null) return null;

        Semester semester = new Semester();
        semester.setId(dto.getId());
        semester.setTerm(Term.valueOf(dto.getTerm()));
        semester.setYear(dto.getYear());
        return semester;
    }
}