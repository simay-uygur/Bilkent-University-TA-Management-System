package com.example.entity.Exams;

import java.util.List;

import com.example.entity.Courses.CourseOffering;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.TransferProctoring;
import com.example.entity.Tasks.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exam_table")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id", unique = true)
    private int examId;

    @Column(name = "description", unique = false, updatable = true)
    private String description; // writing if it is midterm 1, 2 etc (flexible because

    @JsonIgnore
    @OneToOne(
        mappedBy = "exam",  // This refers to the 'exam' field in Task class
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.LAZY,
        optional = false
    )
    private Task task;

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ExamRoom> exam_rooms;

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Swap> swapRequests; // this is used to get the swap requests for this exam

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProctorTaInFaculty> proctorTaInFaculties; // this is used to get the proctor ta in faculties for this exam

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProctorTaFromFaculties> proctorTaFromFaculties; // this is used to get the proctor ta from faculties for this exam

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<TransferProctoring> transferProctoringRequests; // this is used to get the transfer proctoring requests for this exam

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Swap> swapEnableRequests; // this is used to get the swap enable requests for this exam

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<TransferProctoring> transferProctoringRequest; // this is used to get the transfer proctoring requests for this exam

    @Column(name = "swap_enabled", unique = false, updatable = true)
    private boolean swapEnabled; // this is used to enable/disable the swap requests for this exam

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering; // this is used to get the course offering for this exam
}
