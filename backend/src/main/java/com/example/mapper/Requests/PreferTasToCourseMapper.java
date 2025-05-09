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
        dto.setDescription(e.getDescription());
        dto.setRequestType(e.getRequestType());
        dto.setRequestId(e.getRequestId());
        dto.setSentTime(e.getSentTime());
        dto.setSenderName(e.getSender().getName() +" "+ e.getSender().getSurname());
        dto.setInstructorId(e.getSender().getId());
        dto.setReceiverName(e.getReceiver().getName());
        dto.setSectionId(e.getSection().getSectionId());
        dto.setSectionCode(e.getSection().getSectionCode());
        dto.setTaNeeded(e.getTaNeeded());
        dto.setAmountOfAssignedTas(e.getAmountOfAssignedTas());
        String[] parts = e.getSection().getSectionCode().split("-");
        dto.setCourseCode(parts[0] +"-"+ parts[1]);
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
