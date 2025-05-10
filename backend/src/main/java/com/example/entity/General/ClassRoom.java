package com.example.entity.General;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Courses.Lesson;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.Tasks.Task;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    @Column(name = "classroom_id", nullable = false, updatable = false, unique = true)
    private String classroomId;  // A-Z02

    // this is for holding the whole capacity of the classroom
    @Column(name = "class_capacity", nullable = false)
    private int classCapacity;

    @Column(name = "exam_capacity", nullable = false )
    private int examCapacity;

    //this is for holding the capacity according to exam rules - provided in the excel
    @Column(name = "exam_rooms", nullable = false)
    @OneToMany(mappedBy = "examRoom", fetch = FetchType.LAZY)
    private List<ExamRoom> examRooms = new ArrayList<>();

    @OneToMany(
            mappedBy = "lessonRoom",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE, CascadeType.REFRESH}
    )
    private List<Lesson> lessons = new ArrayList<>();

    @OneToOne(mappedBy = "room", fetch = FetchType.LAZY)
    private Task task;

    public boolean isFree(Event event){
        for (Lesson lesson : lessons){
            if (lesson.getDuration().has(event))
                return true;
        }

        for(ExamRoom examRoom : examRooms){
            if (examRoom.getExam().getDuration().has(event))
                return true;
        }

        return false;
    }
}
