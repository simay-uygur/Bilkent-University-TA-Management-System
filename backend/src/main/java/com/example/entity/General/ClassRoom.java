package com.example.entity.General;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Courses.Lesson;
import com.example.entity.Exams.ExamRoom;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "class_room")
@Getter
@Setter
public class ClassRoom {

    @Id
    @Column(name = "classroom_id", nullable = false, updatable = true)
    private String classroomId;

    @Column(name = "class_capacity", nullable = false)
    private int classCapacity;

    @OneToOne(mappedBy = "examRoom")
    private ExamRoom examRoom;

    @OneToMany(
            mappedBy = "lessonRoom",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}
    )
    private List<Lesson> lessons = new ArrayList<>();
}

/*
package com.example.entity.General;


import java.util.ArrayList;
import java.util.List;

import com.example.entity.Courses.Lesson;
import com.example.entity.Exams.ExamRoom;
import com.example.exception.GeneralExc;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "class_room")
public class ClassRoom {

    @Id
    @Column(name = "classroom_id", unique = true, updatable = true, nullable = false)
    private int classroom_id; 

    @Transient
    @Column(nullable = false)
    private String class_code;

    @Column(nullable = false)
    private int class_capacity;

    @OneToOne(mappedBy = "exam_room")
    private ExamRoom section_exam; // this is the section that the class room is related to, not the course

    @OneToMany(
        mappedBy = "lesson_room",  // This refers to the 'lesson_room' field in Lesson class
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}  // No DELETE
    )
    private List<Lesson> lessons = new ArrayList<>();

    @PrePersist
    private void setCourseId() {
        if (this.class_code!= null)
            this.classroom_id = code_to_id(this.class_code);
    }

    public int code_to_id(String to_convert){
        to_convert = to_convert.toUpperCase();
        if (to_convert != null){
            int i = to_convert.indexOf('-') ;
            to_convert = to_convert.substring(0,i) + to_convert.substring(i+1,to_convert.length());
            int number = prefix_to_int(to_convert); 
            return number; 
        }
        return 0; 
    }

    private int prefix_to_int(String prefix){
        String to_return = "" ;
        for(int i = 0; i < prefix.length(); i++){
            int c = prefix.charAt(i) ;
            if (c >= 48 && c <= 57)
                to_return += c - 48 ;
            else if (c >= 'A' && c <= 'Z') {
                to_return += c - 'A' + 1;
            }
            else 
                throw new GeneralExc("Invalid prefix character: " + prefix.charAt(i));
            
        }
        return Integer.parseInt(to_return) ; 
    }
}
*/
