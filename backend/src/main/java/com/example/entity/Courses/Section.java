package com.example.entity.Courses;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.example.entity.Actors.TA;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.Student;
import com.example.entity.Tasks.Task;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "section")
@DynamicUpdate // this is used to update only the changed fields in the database, not the whole object
public class Section {
    @Id
    @Column(name = "section_id", unique = true, updatable = true)
    private int section_id; // cs-319-1 id - > 'c' + 's' + 319 + 1

    @Transient
    private String section_code; // cs-319-1

    @PrePersist
    private void createID(){
        this.section_id = new CourseCodeConverter().code_to_id_sec(this.section_code);
    }

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
        name = "section_tasks",
        joinColumns = @JoinColumn(name = "section_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private ArrayList<Task> section_tasks_list ;

    @OneToMany(
        // this is used to avoid loading the whole exam room when only the section is needed
        fetch = FetchType.LAZY,
        // if the section is deleted, the exam room is also deleted, but if the exam room is deleted, the section is not deleted
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<ExamRoom> exam_rooms;

    @ManyToOne(
        fetch = FetchType.LAZY // this is used to avoid loading the whole course when only the section is needed
    )
    private Course course; // course is the course that the section is related to, not the other way around.

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
        name = "section_students",
        joinColumns = @JoinColumn(name = "section_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> students; // students is the list of students that are in the section, not the other way around.

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
        name = "section_ta_as_students",
        joinColumns = @JoinColumn(name = "section_id"),
        inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> ta_as_students; // tas that take the course as a student

    @OneToMany(
        mappedBy = "section",  // This refers to the 'section' field in Lesson class
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,  // ALL includes DELETE operation
        orphanRemoval = true  // Automatically delete lessons when removed from collection
    )
    private List<Lesson> lessons = new ArrayList<>();
}
