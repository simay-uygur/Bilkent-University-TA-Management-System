package com.example.entity.Exams;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.TA;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Student;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * An individual room assignment for a single exam.
 */
@Entity
@Table(name = "examroom")
@Getter
@Setter
public class ExamRoom {

    @Id
    @Column(name = "examroom_id", nullable = false, updatable = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int examRoomId;          //auto generated

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "classroom_id", nullable = false)
    private ClassRoom examRoom;      // FK kept in the same table (no name override needed)

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name               = "examroom_student_list",
            joinColumns        = @JoinColumn(name = "examroom_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> studentsList = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name               = "examroom_ta_list",
            joinColumns        = @JoinColumn(name = "examroom_id"),
            inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> tasAsStudentsList = new ArrayList<>(); // TODO: change to TA entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    public String getExamRoomCode() {
        return examRoom != null ? examRoom.getClassroomId() : null;
    }
}



/*
package com.example.entity.Exams;

import java.util.List;

import com.example.entity.General.ClassRoom;
import com.example.entity.General.Student;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "examroom")
@Getter
@Setter
public class ExamRoom {

    @Id
    @Column(name = "examroom_id", unique = false, updatable = true, nullable = false)
    private int exarroom_id; // ex. 3191, 3192 etc, where 1 is the exam room number

    @OneToOne(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private ClassRoom exam_room;

    @Column(name = "isApproved", unique = false, updatable = true, nullable = false)
    private boolean isApproved = false;

    @Column(name = "workload", unique = false, updatable = true, nullable = false)
    private int workload;

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE} // course -> deleted => tasks are deleted
    )
    @JoinTable( // creates a table for one to many relationship
        name = "examroom_student_list",
        joinColumns = @JoinColumn(name = "examroom_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id") // creates in ta table field for tasks 
    )
    private List<Student> students_list ;

    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    public String getExamRoom(){
        return this.exam_room.getClass_code();
    }
}
*/
