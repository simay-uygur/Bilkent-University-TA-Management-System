package com.example.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AppLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** INFO | WARN | ERROR */
    @Column(nullable = false, length = 10)
    private String level;

    /** Log kaynağı: LeaveService, TourController … */
    @Column(nullable = false, length = 60)
    private String source;

    /** Asıl mesaj – düz string */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /** Oluşturulma zamanı — otomatik olarak DB tarafında set edilir */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
