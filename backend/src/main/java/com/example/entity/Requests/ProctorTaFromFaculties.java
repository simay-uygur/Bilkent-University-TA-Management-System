package com.example.entity.Requests;

import java.util.List;

import com.example.entity.Exams.Exam;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "proctor_ta_from_faculties_requests")
public class ProctorTaFromFaculties extends Request{
    @OneToMany(
        mappedBy = "proctorTaFromFaculties", 
        cascade= CascadeType.ALL, 
        fetch= FetchType.LAZY, 
        orphanRemoval = true
    )
    List<ProctorTaInFaculty> proctorTaInFaculties;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Exam exam;
}
