package com.example.entity.Requests;

import java.util.List;

import com.example.entity.Actors.TA;
import com.example.entity.Exams.Exam;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "proctor_ta_from_faculties_requests")
public class ProctorTaFromFaculties extends Request{

    @Column(name = "required_tas")
    private int requiredTas;

    @Column(name = "tas_left")
    private int tasLeft;

    @OneToMany(
        mappedBy = "proctorTaFromFaculties", 
        cascade= CascadeType.ALL, 
        fetch= FetchType.LAZY, 
        orphanRemoval = true
    )
    List<ProctorTaInFaculty> proctorTaInFaculties;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

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
