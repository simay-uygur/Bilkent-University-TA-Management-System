package com.example.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;


// com/example/dto/ExamRoomExportDto.java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamRoomExportDto {
    private String               classroomId;
    private int                  capacity;   // examCapacity from ClassRoom
    private List<StudentMiniDto> students;
    private List<StudentMiniDto> tas;
}