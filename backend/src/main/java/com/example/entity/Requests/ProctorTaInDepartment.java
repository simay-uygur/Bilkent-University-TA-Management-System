package com.example.entity.Requests;

import com.example.entity.Actors.Instructor;
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
@Table(name = "proctor_ta_in_department_requests")
public class ProctorTaInDepartment extends Request{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "receiver_department_name",   // the FK column in this table
        referencedColumnName = "name",       // the PK of Department
        nullable = false
    )
    private Department receiver;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "instructor_id", referencedColumnName = "id")
    private Instructor sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id")
    private Exam exam;

    @Column(name = "required_tas")
    private int requiredTas;

    @Column(name = "tas_left")
    private int tasLeft;

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
