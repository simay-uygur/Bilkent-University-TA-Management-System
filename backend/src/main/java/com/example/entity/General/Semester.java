package com.example.entity.General;

import com.example.entity.Courses.CourseOffering;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "semesters",
        uniqueConstraints = @UniqueConstraint(columnNames = {"year", "term"})
)
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
//@Builder
@JsonIgnoreProperties("offerings")   // avoid cycles when serializing
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Term term;    // SPRING, SUMMER, FALL, etc.

    @Column(nullable = false)
    private int year;

    @OneToMany(mappedBy = "semester",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CourseOffering> offerings = new ArrayList<>();
}
