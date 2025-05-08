package com.example.entity.General;

import java.util.List;

import com.example.entity.Actors.DeanOffice;
import com.example.entity.Courses.Department;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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