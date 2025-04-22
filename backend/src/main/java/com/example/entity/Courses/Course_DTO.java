package com.example.entity.Courses;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.Coordinator_DTO;
import com.example.entity.Actors.Instructor_DTO;
import com.example.entity.Actors.TA_DTO;
import com.example.entity.General.Student_DTO;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Course_DTO {
    
    private String course_code;

    private String department;

    private String academical_status;

    private List<Student_DTO> students;
    
    private List<TA_DTO> tas;

    private String[] prereqs;

    private Coordinator_DTO coordinator;

    private List<Instructor_DTO> instructors;

    private List<Section_DTO> sections;

    // Default constructor (required for JPA)
    public Course_DTO() {
        this.students = new ArrayList<>();
        this.tas = new ArrayList<>();
        this.instructors = new ArrayList<>();
        this.sections = new ArrayList<>();
    }

    // Minimal constructor with required fields
    public Course_DTO(String course_code, String department) {
        this();
        this.course_code = course_code;
        this.department = department;
    }

    // Main constructor with all fields
    public Course_DTO(String course_code, String department, String academical_status,
                     List<Student_DTO> students, List<TA_DTO> tas, String prereqs,
                     Coordinator_DTO coordinator, List<Instructor_DTO> instructors,
                     List<Section_DTO> sections) {
        this.course_code = course_code;
        this.department = department;
        this.academical_status = academical_status;
        this.students = students != null ? new ArrayList<>(students) : new ArrayList<>();
        this.tas = tas != null ? new ArrayList<>(tas) : new ArrayList<>();
        this.prereqs = prereqs.split(",");
        this.coordinator = coordinator;
        this.instructors = instructors != null ? new ArrayList<>(instructors) : new ArrayList<>();
        this.sections = sections != null ? new ArrayList<>(sections) : new ArrayList<>();
    }
}
