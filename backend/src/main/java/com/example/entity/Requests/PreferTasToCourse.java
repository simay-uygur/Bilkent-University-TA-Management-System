package com.example.entity.Requests;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Department;
import com.example.entity.Courses.Section;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "prefer_tas_requests")
@Data
public class PreferTasToCourse extends Request{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Instructor sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_name", referencedColumnName = "name", nullable = false)
    private Department receiver;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "request_preferred_tas",
        joinColumns = @JoinColumn(name = "request_id"),
        inverseJoinColumns = @JoinColumn(name = "id")
    )
    private List<TA> preferredTas = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "request_nonpreferred_tas",
        joinColumns = @JoinColumn(name = "request_id"),
        inverseJoinColumns = @JoinColumn(name = "id")
    )
    private List<TA> nonPreferredTas = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false, unique = true)
    private Section section;

    @Column(name = "ta_needed")
    private int taNeeded;

    @Column(name = "amount_of_assigned_ta")
    private int amountOfAssignedTas = 0;

    public void setDepartmentName(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDepartmentName'");
    }

    public void setInstructorId(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setInstructorId'");
    }
}
