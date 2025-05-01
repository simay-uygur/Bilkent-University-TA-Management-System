package com.example.entity.General;

import com.example.entity.Actors.DeanOffice;
import com.example.entity.Courses.Department;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@Table(name = "faculties")         // e.g. “Engineering”, “Science”, “Art & Design”
public class Faculty {

    @Id
    @Column(length = 64)           // keep it short but unique (ex: “ENG”, “SCI”)
    private String code;

    private String title;          // full display name – “Faculty of Engineering”

    /** Dean’s office (optional, if you decide to store it here) */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "dean_office_id")
    private DeanOffice deanOffice; // create a simple entity or embed a value object

    /** Bidirectional link back to departments */
    @OneToMany(mappedBy = "faculty",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Department> departments;
}