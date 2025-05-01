package com.example.repo;


import com.example.entity.Actors.DeanOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeanOfficeRepo extends JpaRepository<DeanOffice, Long> {

}