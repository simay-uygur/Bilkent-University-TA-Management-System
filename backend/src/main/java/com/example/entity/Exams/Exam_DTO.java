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

    public void setExam_rooms(List<ExamRoom_DTO> rooms) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
