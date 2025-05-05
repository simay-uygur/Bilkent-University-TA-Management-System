package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.entity.General.Event;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDto {
    private Event    duration;
    private String courseCode;
    private String       type;
    private List<ExamRoomDto> examRooms;
    private int   requiredTas;
}
