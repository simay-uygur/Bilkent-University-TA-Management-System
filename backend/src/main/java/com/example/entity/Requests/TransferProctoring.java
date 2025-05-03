package com.example.entity.Requests;

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
@Table(name = "transfer_proctoring_requests")
public class TransferProctoring extends Request{
    @Column(name = "required_tas", unique = false, updatable = true)
    private int requiredTas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id")
    private Exam exam;
}
