package com.example.entity.Actors;

import com.example.entity.Courses.Course;
import com.example.entity.Courses.Section;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.ProctorType;
import com.example.entity.Tasks.TaTask;
import com.example.exception.NoPersistExc;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Teaching-Assistant entity.
 * Field names follow Java camel-case conventions; column names keep their original
 * snake-case via {@code @Column(name = "...")}, so no DB migration is required.
 */
@Entity
@Table(name = "TA")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TA extends User {

    /* ─────────────── basic attributes ─────────────── */

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_level", nullable = false)
    private AcademicLevelType academicLevel;

    @Column(name = "total_workload", nullable = false)
    private int totalWorkload = 0;

    @Column(name = "is_active", updatable = false, nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "ta_type", nullable = false)
    private TAType taType;

    @Column(name = "department", nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "proctor_type")
    private ProctorType proctorType = ProctorType.ALL_COURSES;

    @Column(name = "is_graduated", nullable = false)
    private Boolean isGraduated = false;

    /* ─────────────── relationships ─────────────── */

    /** Courses for which this user is an official TA */
    @ManyToMany(
            mappedBy = "courseTas",
            fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }
    )
    private List<Course> courses = new ArrayList<>();

    /** Sections the TA attends as a student */
    @ManyToMany(
            mappedBy = "taAsStudents",
            fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }
    )
    private List<Section> tasOwnLessons = new ArrayList<>();

    /** Individual tasks (grading, proctoring, etc.) */
    @OneToMany(mappedBy = "taOwner", cascade = CascadeType.ALL)
    private List<TaTask> taTasks = new ArrayList<>();

    /* ─────────────── helpers ─────────────── */

    public void increaseWorkload(int load) {
        totalWorkload += load;
    }

    public void decreaseWorkload(int load) {
        if (totalWorkload - load < 0) {
            throw new NoPersistExc("Decrease workload error: workload cannot be negative!");
        }
        totalWorkload -= load;
    }

    /* ─────────────── equality ─────────────── */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TA)) return false;
        TA other = (TA) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}

/*
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
        mappedBy = "courseTas", // the other side of the relationship is the owner of the relationship
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

    @Column(name = "is_graduated", nullable = false)
    private Boolean isGraduated = false;

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
*/
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
