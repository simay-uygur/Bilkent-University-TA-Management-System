package com.example.entity.Exams;

import java.util.List;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Setter
@Getter
public class Exam_DTO {
    
    private String duration;

    private String course;

    private List<ExamRoom_DTO> exam_rooms;
}
