package com.example.service;


import com.example.entity.General.Semester;
import com.example.repo.SemesterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterServImpl implements SemesterServ {
    private final SemesterRepo repo;

    @Override
    public Semester create(Semester semester) {
        return repo.save(semester);
    }

    @Override
    public Semester update(Integer id, Semester semester) {
        Semester existing = getById(Long.valueOf(id));
        existing.setTerm(semester.getTerm());
        existing.setYear(semester.getYear());
        existing.setOfferings(semester.getOfferings());
        return repo.save(existing);
    }

    @Override
    public Semester getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found: " + id));
    }

    @Override
    public List<Semester> getAll() {
        return repo.findAll();
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

//package com.example.service;
//
//
//import com.example.entity.General.Semester;
//import com.example.repo.SemesterRepo;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class SemesterServImpl implements SemesterServ {
//
//    private final SemesterRepo repo;
//
//    @Override
//    public Semester create(Semester semester) {
//        return repo.save(semester);
//    }
//
//    @Override
//    public Semester update(Integer id, Semester semester) {
//        Semester existing = getById(id);
//        existing.setTerm(semester.getTerm());
//        existing.setYear(semester.getYear());
//        existing.setOfferings(semester.getOfferings());
//        return repo.save(existing);
//    }
//
//    @Override
//    public Semester getById(Integer id) {
//        return repo.findById(Long.valueOf(id)) //hope works
//                .orElseThrow(() -> new IllegalArgumentException("Semester not found: " + id));
//    }
//
//    @Override
//    public List<Semester> getAll() {
//        return repo.findAll();
//    }
//
//    @Override
//    public void delete(Integer id) {
//        repo.deleteById(Long.valueOf(id));
//    }
//}