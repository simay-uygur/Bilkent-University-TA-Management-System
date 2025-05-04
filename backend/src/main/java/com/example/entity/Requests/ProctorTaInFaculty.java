package com.example.entity.Requests;

import com.example.entity.Exams.Exam;
import com.example.entity.General.Faculty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "proctor_ta_in_faculty_requests")
public class ProctorTaInFaculty extends Request{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", referencedColumnName = "faculty_id")
    private Faculty faculty;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id")
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proctor_ta_from_faculties_id", referencedColumnName = "request_id")
    private ProctorTaFromFaculties proctorTaFromFaculties;
}
