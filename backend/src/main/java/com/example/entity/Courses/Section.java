package com.example.entity.Courses;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.example.entity.Curriculum.Lesson;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.Event;
import com.example.entity.Tasks.Task;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
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

    @Embedded
    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "section_schedule", unique = true, updatable = true, nullable = false)
    private List<Event> schedule ;

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

    @OneToOne(
        fetch = FetchType.LAZY, // this is used to avoid loading the whole duty when only the section is needed
        cascade = {CascadeType.PERSIST, CascadeType.MERGE} 
        // if the section is deleted, the duty is also deleted, but if the duty is deleted, the section is not deleted
    )
    @JoinColumn(name = "duty_id", referencedColumnName = "duty_id")
    private Lesson duty; // duty is the duty that the section is related to, not the other way around.
}
