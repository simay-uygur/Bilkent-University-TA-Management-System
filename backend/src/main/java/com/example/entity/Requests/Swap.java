package com.example.entity.Requests;

import com.example.entity.Actors.TA;
import com.example.entity.Exams.Exam;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@Table(name = "swap_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // ← add this line
@AllArgsConstructor
public class Swap extends Request {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_ta_id", referencedColumnName = "id")
    private TA sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_ta_id", referencedColumnName = "id")
    private TA receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_exam_id", referencedColumnName = "exam_id")
    private Exam sendersExam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_exam_id", referencedColumnName = "exam_id")
    private Exam receiversExam;

    public Swap(Exam exam, Exam exam1){
        this.sendersExam = exam;
        this.receiversExam = exam1;
    }
}
