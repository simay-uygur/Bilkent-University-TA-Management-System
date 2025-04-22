package com.example.entity.General;

import org.springframework.ui.context.ThemeSource;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Student_DTO {
    
    private String name ;

    private String surname ;

    private int student_id ;

    public Student_DTO(String name, String surname, int id){
        this.name = name;
        this.surname = surname;
        this.student_id = id;
    }

    @Override
    public String toString(){
        return "Student: " + name + " " + surname + ":" + student_id;
    }
}
