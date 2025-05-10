package com.example.mapper;

import org.springframework.stereotype.Component;

import com.example.dto.ExamDto;
import com.example.entity.Exams.Exam;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class ExamMapper {
    // inside your ExamServiceImpl (or a Mapper util class)
    public ExamDto toDto(Exam e) {
        return new ExamDto(
                e.getExamId(),
                e.getDuration(),
                e.getCourseOffering().getCourse().getCourseCode(),   // "CS-315"
                e.getDescription(),                      // "Midterm 1"
                e.getExamRooms()
                .stream()
                .map(room -> room.getExamRoomCode())        // e.g. "B-206"
                .toList(),
                e.getRequiredTAs(),
                e.getWorkload());
    }

}
