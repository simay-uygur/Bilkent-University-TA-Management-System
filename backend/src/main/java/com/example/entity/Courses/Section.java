package com.example.entity.Courses;

import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.Student;
import com.example.entity.Tasks.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Section — a single class group of a course (e.g. CS-319-1).
 * <p>
 * • Each section belongs to one {@link Course}.<br>
 * • A section can have <strong>one or more instructors</strong> &amp; several TAs.<br>
 * • Relationships keep the original table / column names, even though field names are camel-case.
 */
@Entity
@Table(name = "section")
@DynamicUpdate
@Getter
@Setter
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id", unique = true, nullable = false)
    private Long sectionId;                                       // e.g. 3193191

    @Column(name = "section_code", nullable = false, unique = true) // CS-319-1
    private String sectionCode;

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name               = "section_tasks",
            joinColumns        = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private List<Task> sectionTasksList = new ArrayList<>();

    //instead of course, now course offering is being used
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    private CourseOffering offering;

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name               = "section_students",
            joinColumns        = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> registeredStudents = new ArrayList<>();

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "section_ta_registrations",
            joinColumns = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> registeredTas = new ArrayList<>(); // ta's which are taking this course (registered)

    /** TAs who are *assigned* to run or assist this section */
    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "section_ta_assignments",
            joinColumns = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "ta_id")
    )
    private List<TA> assignedTas = new ArrayList<>(); // ta's which are assigned to this section/course to help

    @ManyToOne(fetch = LAZY)
    // cascade = { CascadeType.PERSIST, CascadeType.MERGE }) - not to add the instructor
    @JoinColumn(name = "instructor_id", nullable = false) //nullable is false be careful!
    private Instructor instructor;

    @OneToMany(mappedBy      = "section",
            fetch         = LAZY,
            cascade       = CascadeType.ALL,
            orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(
            mappedBy      = "section",
            fetch         = FetchType.LAZY,
            cascade       = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Task> tasks = new ArrayList<>();

    

}
