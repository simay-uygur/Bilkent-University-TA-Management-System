package com.example.service;

import com.example.dto.DeanOfficeDto;
import com.example.entity.Actors.DeanOffice;
import com.example.entity.Actors.Role;
import com.example.entity.General.Faculty;
import com.example.mapper.DeanOfficeMapper;
import com.example.repo.DeanOfficeRepo;
import com.example.repo.FacultyRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeanOfficeServImpl implements DeanOfficeServ {
    private final DeanOfficeMapper deanOfficeMapper;
    private final DeanOfficeRepo deanOfficeRepo;
    private final FacultyRepo facultyRepo;
    private final PasswordEncoder encoder;
    

    @Override
    @Transactional
    public DeanOffice save(DeanOffice deanOffice, String facultyCode) {
        // encode & role
        deanOffice.setPassword(encoder.encode(deanOffice.getPassword()));
        deanOffice.setRole(Role.DEANS_OFFICE);

        // fetch the faculty (owning side)
        Faculty faculty = facultyRepo.findById(facultyCode)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));

        // wire up both sides
        deanOffice.setFaculty(faculty);
        faculty.setDeanOffice(deanOffice);

        // now saving the faculty will cascade to persist the DeanOffice
        return facultyRepo.save(faculty)
                .getDeanOffice();
    }

    /* @Override
    public List<DeanOffice> getAll() {
        return deanOfficeRepo.findAll();
    } */
    @Override
    public List<DeanOfficeDto> getAll() {
        List<DeanOffice> deanOffices = deanOfficeRepo.findAll();
        return deanOffices.stream()
                .map(deanOfficeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeanOffice getById(Long id) {
        return deanOfficeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DeanOffice not found"));
    }

    @Override
    public void deleteById(Long id) {
        deanOfficeRepo.deleteById(id);
    }

    @Override
    public DeanOffice saveFromDto(DeanOfficeDto deanOfficeDto, String facultyCode) {
        Faculty faculty = facultyRepo.findById(facultyCode)
                .orElseThrow(() -> new IllegalArgumentException("…"));
        DeanOffice deanOffice = deanOfficeMapper.toEntity(deanOfficeDto, faculty);
        return deanOfficeRepo.save(deanOffice);
    }
}