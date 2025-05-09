package com.example.entity.Courses;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.example.entity.General.AcademicLevelType;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Table(name = "course")
@DynamicUpdate // this is used to update only the changed fields in the database, not the whole object
public class Course {
    @Id
    @Column(name = "course_id", unique = true, updatable = true) // make updatable false
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int courseId;
    
    @Column(name = "course_code", unique = true)
    private String courseCode; // cs-319

    @Column(name = "course_name", unique = false, updatable = true, nullable = false)
    //@NotEmpty(message = "The field can not be empty!")
    private String courseName;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_academic_status", updatable = true, nullable = false)
    private AcademicLevelType courseAcademicStatus; //bs, ms, phd

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="department_name", nullable=false) // added now
    private Department department;

    //there should be exam class

    @Column(name = "prereq_list", unique = false, updatable = true, nullable = false)
    //@NotEmpty(message = "The field can not be empty!")
    private String prereqList; //course prerequisites

    //instead of sections, there are course offerings
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseOffering> courseOfferings = new ArrayList<>();

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj ;
        return course.getCourseId() == this.courseId;
    }

    public int getCapacity(){
        int capacity = 0;
        for (CourseOffering courseOffering : courseOfferings) {
            for (Section section : courseOffering.getSections()) {
                capacity += section.getRegisteredStudents().size();
                capacity += section.getRegisteredTas().size();
            }
        }
        return capacity;
    }
    //coordinator- deleted now only instructors
}