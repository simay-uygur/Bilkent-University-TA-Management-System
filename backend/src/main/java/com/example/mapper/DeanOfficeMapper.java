package com.example.mapper;

import com.example.dto.DeanOfficeDto;
import com.example.entity.Actors.DeanOffice;
import com.example.entity.General.Faculty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeanOfficeMapper {

    public DeanOfficeDto toDto(DeanOffice deanOffice) {
        if (deanOffice == null) return null;

        String facultyCode = deanOffice.getFaculty() != null
                ? deanOffice.getFaculty().getCode()
                : null;

        return new DeanOfficeDto(
                deanOffice.getId(),
                deanOffice.getName(),
                deanOffice.getSurname(),
                deanOffice.getWebmail(),
                deanOffice.getFaculty().getCode()
        );
    }

    public DeanOffice toEntity(DeanOfficeDto dto, Faculty faculty) {
        if (dto == null) return null;

        DeanOffice dean = new DeanOffice();
        dean.setId(dto.getId());
        dean.setName(dto.getName());
        dean.setSurname(dto.getSurname());
        dean.setWebmail(dto.getWebmail());
        dean.setFaculty(faculty);
        // Set default values for inherited fields
        dean.setPassword("default123");
        dean.setRole(com.example.entity.Actors.Role.DEANS_OFFICE);
        dean.setDeleted(false);
        return dean;
    }
}