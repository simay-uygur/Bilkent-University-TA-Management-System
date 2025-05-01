package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Lesson entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    /**
     * The duration of the lesson, typically represented as a string (e.g., "09:00-10:00").
     */
    private String duration;

    /**
     * The classroom or room identifier where the lesson takes place.
     */
    private String room;
}