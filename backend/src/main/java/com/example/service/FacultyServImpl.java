package com.example.service;


import com.example.entity.General.Faculty;
import com.example.repo.FacultyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyServImpl implements FacultyServ {

    private final FacultyRepo facultyRepo;

    @Override
    public Faculty save(Faculty faculty) {
        return facultyRepo.save(faculty);
    }

    @Override
    public List<Faculty> getAll() {
        return facultyRepo.findAll();
    }

    @Override
    public Faculty getByCode(String code) {
        return facultyRepo.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found: " + code));
    }

    @Override
    public void deleteByCode(String code) {
        facultyRepo.deleteById(code);
    }
}

