package com.example.entity.Actors;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
import com.example.entity.General.AcademicLevelType;
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

    @Column(name = "proctor_type", unique = false, updatable = true)
    private ProctorType proctor_type = ProctorType.ALL_COURSES;
}
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