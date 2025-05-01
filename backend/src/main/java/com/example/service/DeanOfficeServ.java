package com.example.service;


import com.example.dto.DeanOfficeDto;
import com.example.entity.Actors.DeanOffice;
import java.util.List;

public interface DeanOfficeServ {
    DeanOffice save(DeanOffice deanOffice, String facultyCode);   // attach to faculty
    List<DeanOffice> getAll();
    DeanOffice getById(Long id);
    void deleteById(Long id);

    DeanOffice saveFromDto(DeanOfficeDto deanOfficeDto, String facultyCode);
}