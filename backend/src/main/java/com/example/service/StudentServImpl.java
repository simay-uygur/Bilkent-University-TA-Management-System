package com.example.service;

import org.springframework.stereotype.Service;

import com.example.entity.General.Student;
import com.example.repo.StudentRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class StudentServImpl implements StudentServ{

    private final StudentRepo studRepo;

    @Override
    public boolean createStudent(Student stud) {
        studRepo.save(stud);
        return true;
    }
    
}
