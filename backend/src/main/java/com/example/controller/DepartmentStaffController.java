package com.example.controller;

import com.example.dto.DepartmentStaffDto;
import com.example.dto.InstructorDto;
import com.example.entity.Actors.DepartmentStaff;
import com.example.service.DepartmentStaffServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department-staff")
@RequiredArgsConstructor
public class DepartmentStaffController {

    private final DepartmentStaffServ staffServ;

    @PostMapping
    public ResponseEntity<DepartmentStaff> create(@RequestBody DepartmentStaff staff) {
        return ResponseEntity.ok(staffServ.createDepartmentStaff(staff));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentStaff> update(@PathVariable Long id, @RequestBody DepartmentStaff staff) {
        return ResponseEntity.ok(staffServ.updateDepartmentStaff(id, staff));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        staffServ.deleteDepartmentStaff(id);
        return ResponseEntity.ok("Deleted successfully");
    }
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentStaffDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(staffServ.getDepartmentStaffById(id));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentStaffDto>> getAll() {
        return ResponseEntity.ok(staffServ.getAllDepartmentStaff());
    }
    /*  @GetMapping("/department/{deptName}")
    public ResponseEntity<List<InstructorDto>> getByDepartment(
            @PathVariable("deptName") String deptName
    ) {
        List<InstructorDto> dtos = staffServ.getInstructorsByDepartment(deptName);
        return ResponseEntity.ok(dtos);
    } */
}