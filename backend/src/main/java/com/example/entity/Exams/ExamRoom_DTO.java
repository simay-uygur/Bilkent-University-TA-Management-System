package com.example.entity.Exams;

import java.util.List;

import com.example.entity.General.Student_DTO;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ExamRoom_DTO {
    
    private String room;

    private List<Student_DTO> students;

    public ExamRoom_DTO(String room, List<Student_DTO> students){
        this.room = room;
        this.students = students;
    }
    
    public ExamRoom_DTO(){}
}

