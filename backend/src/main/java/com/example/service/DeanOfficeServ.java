package com.example.service;


import com.example.dto.DeanOfficeDto;
import com.example.dto.ExamDto;
import com.example.dto.FacultyCourseDto;
import com.example.dto.FacultyCourseOfferingsDto;
import com.example.entity.Actors.DeanOffice;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DeanOfficeServ {
    DeanOffice save(DeanOffice deanOffice, String facultyCode);   // attach to faculty
    //List<DeanOffice> getAll();
    List<DeanOfficeDto> getAll();
    DeanOffice getById(Long id);
    void deleteById(Long id);
    DeanOffice saveFromDto(DeanOfficeDto deanOfficeDto, String facultyCode);
    FacultyCourseOfferingsDto getFacultyCourseData(String facultyCode);

    @Transactional(readOnly = true)
    List<ExamDto> getAllExamsForFaculty(String facultyCode);

    @Transactional(readOnly = true)
    ExamDto getExamDetails(Integer examId);
    FacultyCourseDto getFacultynormalCourseData(String facultyCode);
}