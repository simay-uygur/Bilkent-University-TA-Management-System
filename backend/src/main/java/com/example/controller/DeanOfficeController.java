
package com.example.controller;

import com.example.entity.Actors.DeanOffice;
import com.example.service.DeanOfficeServ;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dean-offices")
@RequiredArgsConstructor
public class DeanOfficeController {

    private final DeanOfficeServ deanOfficeServ;

    @PostMapping("/{facultyCode}")
    public DeanOffice create(@RequestBody DeanOffice deanOffice,
                             @PathVariable String facultyCode) {
        return deanOfficeServ.save(deanOffice, facultyCode);
    }

    @GetMapping
    public List<DeanOffice> list() {
        return deanOfficeServ.getAll();
    }

    @GetMapping("/{id}")
    public DeanOffice byId(@PathVariable Long id) {
        return deanOfficeServ.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deanOfficeServ.deleteById(id);
    }
}