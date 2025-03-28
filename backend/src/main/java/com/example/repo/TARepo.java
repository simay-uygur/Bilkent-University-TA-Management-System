package com.example.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.entity.TA;

public interface TARepo extends JpaRepository<TA, Long> { // TA is the entity and Long is the type of the primary key
    @Query("SELECT t FROM TA t WHERE t.isDeleted = false")
    List<TA> findAllTAs(); // fixed query
}