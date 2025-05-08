package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    private EventDto duration;
    private String classroomId;  // e.g., "EA-101"
    private String lessonType;   // e.g., "LESSON" or "SPARE_HOUR"
    private String sectionId;    // Required for lesson creation
    private String day;          // e.g., "MONDAY" — assumed added earlier
}

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class LessonDto {
//
//    private EventDto duration;
//    private String classroomId;  // e.g., "EA-101"
//    private Integer examCapacity;
//    private String lessonType;  // e.g., "LESSON" or "SPARE_HOUR"
//    private String sectionId;   // Required for lesson creation
//}
//// exam capacity is not necessary (it can be deleted)   and from the request both classroomid and exam capacity is received as null

/*private Event duration;
    private String classCode;  // e.g. "A101"
    private String room;   */