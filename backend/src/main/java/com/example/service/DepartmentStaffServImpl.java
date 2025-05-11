package com.example.service;

import com.example.dto.DepartmentStaffDto;
import com.example.entity.Actors.DepartmentStaff;
import com.example.entity.Actors.Role;
import com.example.mapper.DepartmentStaffMapper;
import com.example.repo.DepartmentStaffRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentStaffServImpl implements DepartmentStaffServ {

    private final DepartmentStaffRepo repo;
    private final PasswordEncoder encoder;
    private final DepartmentStaffMapper mapper;
    private final LogService log;
    @Override
    public DepartmentStaffDto getDepartmentStaffById(Long id) {
        DepartmentStaff staff = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff with id " + id + " not found."));
        return mapper.toDto(staff);
    }
    @Override
    public DepartmentStaff createDepartmentStaff(DepartmentStaff staff) {
        if (repo.existsById(staff.getId())) {
            throw new RuntimeException("Staff with id " + staff.getId() + " already exists.");
        }

        staff.setRole(Role.DEPARTMENT_STAFF);
        staff.setPassword(encoder.encode(staff.getPassword()));
        log.info("Department Staff creation", "New Department Staff with id: " + staff.getId() + " is created");
        return repo.save(staff);
    }

    @Override
    public boolean deleteDepartmentStaff(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Staff with id " + id + " not found.");
        }
        log.info("Department Staff deletion", "Department Staff with id: " +id+ " is deleted from the system.");
        repo.deleteById(id);
        return true;
    }

    @Override
    public DepartmentStaff updateDepartmentStaff(Long id, DepartmentStaff staff) {
        DepartmentStaff existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff with id " + id + " not found."));

        existing.setName(staff.getName());
        existing.setSurname(staff.getSurname());
        existing.setWebmail(staff.getWebmail());
        existing.setIsActive(staff.getIsActive());
        existing.setDepartment(staff.getDepartment());
        log.info("Department Staff update", "Department Staff with id: " + id + " is updated.");
        return repo.save(existing);
    }

    @Override
    public List<DepartmentStaffDto> getAllDepartmentStaff() {
        List<DepartmentStaff> staffList = repo.findAll();
        return staffList.stream()
                .map(mapper::toDto)
                .toList();
    }
}