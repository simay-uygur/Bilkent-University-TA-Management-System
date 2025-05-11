package com.example.entity.Requests;

import com.example.entity.Actors.DeanOffice;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Department;
import com.example.entity.Exams.Exam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "proctor_ta_in_faculty_requests")
public class ProctorTaInFaculty extends Request{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deans_id", referencedColumnName = "id")
    private DeanOffice receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_dep", referencedColumnName = "name")
    private Department sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id")
    private Exam exam;

    @Column(name = "required_tas")
    private int requiredTas;

    @Column(name = "tas_left")
    private int tasLeft;

    @Column(name = "instr_id")
    private Long instrId;

    public boolean addTa(TA ta){
        if (exam.incr()){
            exam.getAssignedTas().add(ta);
            tasLeft--;
            return true;
        }
        return false;
    }

    public boolean removeTa(TA ta){
        if (exam.decr()){
            exam.getAssignedTas().remove(ta);
            tasLeft++;
            return true;
        }
        return false;
    }
}
