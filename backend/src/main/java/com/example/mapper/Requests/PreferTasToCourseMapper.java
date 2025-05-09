package com.example.mapper.Requests;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.entity.Requests.PreferTasToCourse;
import com.example.entity.Requests.PreferTasToCourseDto;
import com.example.entity.Requests.PreferTasToCourseDto.TaInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PreferTasToCourseMapper {
    public PreferTasToCourseDto toDto(PreferTasToCourse e){
        PreferTasToCourseDto dto = new PreferTasToCourseDto();
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setRequestId(e.getRequestId());
        dto.setRequesType(e.getRequestType());
        dto.setDepartmentName(e.getReceiver().getName());
        dto.setInstructorId(e.getSender().getId());
        dto.setInstructorName(e.getSender().getName());
        dto.setInstructorSurname(e.getSender().getSurname());
        dto.setSectionId(e.getSection().getSectionId());
        dto.setSectionCode(e.getSection().getSectionCode());
        dto.setTaNeeded(e.getTaNeeded());
        dto.setAmountOfAssignedTas(e.getAmountOfAssignedTas());
        dto.setPreferredTas(e.getPreferredTas().stream()
            .map(t -> {
                TaInfo ti = new TaInfo();
                ti.setId(t.getId());
                ti.setName(t.getName());
                ti.setSurname(t.getSurname());
                return ti;
            })
            .collect(Collectors.toList())
        );

        dto.setNonPreferredTas(e.getNonPreferredTas().stream()
            .map(t -> {
                TaInfo ti = new TaInfo();
                ti.setId(t.getId());
                ti.setName(t.getName());
                ti.setSurname(t.getSurname());
                return ti;
            })
            .collect(Collectors.toList())
        );

        return dto;
    }

}
