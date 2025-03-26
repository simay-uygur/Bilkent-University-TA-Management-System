package com.example.demo1.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo1.entity.TA;

public interface TARepo extends JpaRepository<TA, Long> { // TA is the entity and Long is the type of the primary key
    @Query("SELECT ta FROM TA ta") 
    List<TA> findAllTAs(); // ama calismiyor
}