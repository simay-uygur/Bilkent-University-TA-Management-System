package com.example.entity.Actors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.DynamicUpdate;

import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.ProctorType;
import com.example.entity.Tasks.TA_Task;
import com.example.exception.NoPersistExc;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DynamicUpdate
@Getter
@Setter
@Table(name = "TA")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TA extends User{
    @Column(name = "academic_level", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private AcademicLevelType academic_level ; 

    @Column(name = "total_workload", unique = false, updatable = true, nullable = false)
    private int total_workload = 0; // toplam iş yükü

    @Column(name = "is_active", updatable = false,  nullable = false)  //added new
    private Boolean isActive = true;
    
    @Column(name = "ta_type", unique = false, updatable = true, nullable = false)
    private TAType ta_type;

    @Column(name = "department", nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "proctor_type")
    private ProctorType proctorType = ProctorType.ALL_COURSES;

    @ManyToMany(
        mappedBy = "course_tas", // the other side of the relationship is the owner of the relationship
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH} // cascade operations for the relationship
    )
    private List<Course> courses = new ArrayList<>(); // kurslarin ta'ları

    @ManyToMany(
        mappedBy = "ta_as_students", // the other side of the relationship is the owner of the relationship
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH} // cascade operations for the relationship
    )
    private List<Section> tas_own_lessons = new ArrayList<>(); // ta'nın dersleri

    public void insreaseWorkLoad(int load){
        total_workload += load ;
    }

    @OneToMany(mappedBy = "ta_owner", cascade = CascadeType.ALL)
    private List<TA_Task> ta_tasks = new ArrayList<>(); 

    public void decreaseWorkLoad(int load){
        if (total_workload - load < 0)
            throw new NoPersistExc("Decrease workload error: workload can not be negative!\nExecution ") ;
        total_workload -= load ;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;                          // same reference
        if (!(o instanceof TA)) return false;                // null or different type
        TA other = (TA) o;
        // if either ID is null, fall back to identity equality
        if (getId() == null || other.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), other.getId());
    }
}

//json should be changed
/*{
    "role" : "TA",
    "id" : 1, 
    "password" : "1", 
    "name" : "ta",
    "surname" : "1",
    "webmail" : "ta.1@ug.bilkent.edu.tr",
    "type" : "TA",
    "academic_level" : "MS"
} */