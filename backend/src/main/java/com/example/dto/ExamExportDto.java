package com.example.dto;


import com.example.entity.General.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamExportDto {
    private Integer                 examId;
    private Event                   duration;
    private String                  description;
    private List<ExamRoomExportDto> rooms;
}