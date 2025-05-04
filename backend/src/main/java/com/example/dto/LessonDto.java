package com.example.dto;

import lombok.*;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {

    private String duration;
    private String classCode;  // e.g. "A101"
    private String room;           // classroomId
    private Integer examCapacity;  // new field for display
}

