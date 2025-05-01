package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDto {
    private String duration;
    private String courseCode;
    private List<ExamRoomDto> examRooms;
}
