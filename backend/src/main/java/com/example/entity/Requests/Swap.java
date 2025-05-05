package com.example.entity.Requests;

import com.example.entity.Exams.Exam;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@Table(name = "swap_requests")
@AllArgsConstructor
public class Swap extends Request {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id")
    private Exam exam;
}
