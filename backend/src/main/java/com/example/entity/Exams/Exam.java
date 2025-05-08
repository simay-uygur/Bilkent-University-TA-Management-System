package com.example.entity.Exams;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.General.Event;
import com.example.entity.General.Student;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromOtherFaculty;
import com.example.entity.Requests.ProctorTaInDepartment;
import com.example.entity.Requests.Swap;
import com.example.entity.Requests.TransferProctoring;
import com.fasterxml.jackson.annotation.JsonInclude;

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
import jakarta.persistence.OneToMany;
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
    @Column(name = "exam_id", unique = true)
    private Integer examId;

    @Column(name = "description", unique = false, updatable = true)
    private String description; // writing if it is midterm 1, 2 etc (flexible because

    @Column(name = "workload", nullable = false)
    private Integer workload = 4;

    @Column(name = "duration", nullable = false)
    private Event duration; // this is used to get the duration of the exam

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ExamRoom> examRooms;

    
    @ManyToMany(
        fetch  = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.MERGE }
        )
        @JoinTable(
            name               = "exam_tas_as_proctors",
            joinColumns        = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "ta_id")
            )
            private List<TA> assignedTas; // this is used to get the tas for this exam
            
    @ManyToMany(
      fetch  = FetchType.LAZY,
      cascade = { CascadeType.PERSIST, CascadeType.MERGE }
    )
    @JoinTable(
      name               = "exam_students_as_proctors",
      joinColumns        = @JoinColumn(name = "exam_id"),
      inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> assignedStudents = new ArrayList<>();

    /*Requests*/
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
    private List<ProctorTaInDepartment> proctorTaInDepartment; // this is used to get the proctor ta in faculties for this exam
        
    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProctorTaFromFaculties> proctorTaFromFaculties;

    @OneToMany(
        mappedBy = "exam",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProctorTaFromOtherFaculty> proctorTaFromOtherFaculty;
    
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
    private List<TransferProctoring> transferProctoringRequest; // this is used to get the transfer proctoring requests for this exam

    /*-------*/

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering; // this is used to get the course offering for this exam

    @Column(name = "is_active", updatable = true)
    private Boolean isActive = true;

    @Column(name = "required_tas", unique = false, updatable = true, nullable = false)
    private Integer requiredTAs; // this is used to get the required tas for this exam

    @Column(name = "amount_of_assigned_tas", unique = false, updatable = true)
    private Integer amountOfAssignedTAs = 0; // this is used to get the assigned tas for this exam

    public boolean incr(){
        if (amountOfAssignedTAs != requiredTAs)
        {
            amountOfAssignedTAs++;
            return true;
        }
        return false;
    }

    public boolean decr(){
        if (amountOfAssignedTAs > 0)
        {
            amountOfAssignedTAs--;
            return true;
        }
        return false;
    }
}
