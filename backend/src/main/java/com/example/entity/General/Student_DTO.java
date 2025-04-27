package com.example.entity.General;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Student_DTO {
    
    private String name ;

    private String surname ;

    private Long student_id ;

    public Student_DTO(Long id, String name, String surname){
        this.name = name;
        this.surname = surname;
        this.student_id = id;
    }

    @Override
    public String toString(){
        return "Student: " + name + " " + surname + ":" + student_id;
    }
}
