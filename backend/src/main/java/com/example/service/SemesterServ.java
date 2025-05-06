package com.example.service;


import com.example.entity.General.Semester;

import java.util.List;
import java.util.Optional;

public interface SemesterServ {
    Semester create(Semester semester);
    Semester update(Integer id, Semester semester);
    Semester getById(Long id);
    List<Semester> getAll();
    void delete(Long id);
    Optional<Semester> findByYearAndTerm(int year, String term);
}