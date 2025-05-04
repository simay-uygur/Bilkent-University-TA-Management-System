package com.example.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoomDto {
    private String classroomId;
    private int classCapacity;
    private int examCapacity;
}