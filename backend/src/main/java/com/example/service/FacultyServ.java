package com.example.service;


import com.example.entity.General.Faculty;
import java.util.List;

public interface FacultyServ {
    Faculty save(Faculty faculty);
    List<Faculty> getAll();
    Faculty getByCode(String code);
    void deleteByCode(String code);
}

