package com.example.dto;

import com.example.entity.General.Event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {

    private Event duration;
    private String classCode;  // e.g. "A101"
    private String room;           // classroomId
    private Integer examCapacity;  // new field for display
}

