package com.example.entity.Requests;

import com.example.entity.Actors.DeanOffice;
import com.example.entity.Exams.Exam;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "proctor_ta_from_other_faculty_requests")
public class ProctorTaFromOtherFaculty extends Request{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sended_fac_code", referencedColumnName = "id")
    private DeanOffice sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_fac_code", referencedColumnName = "id")
    private DeanOffice receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id")
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proctor_ta_from_faculties_id", referencedColumnName = "request_id")
    private ProctorTaFromFaculties proctorTaFromFaculties;
}