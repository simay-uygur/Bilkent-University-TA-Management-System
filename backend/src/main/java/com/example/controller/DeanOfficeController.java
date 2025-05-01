package com.example.controller;

import com.example.dto.DeanOfficeDto;
import com.example.entity.Actors.DeanOffice;
import com.example.mapper.DeanOfficeMapper;
import com.example.service.DeanOfficeServ;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dean-offices")
@RequiredArgsConstructor
public class DeanOfficeController {

    private final DeanOfficeServ deanOfficeServ;

//    @PostMapping("/{facultyCode}")
//    public DeanOfficeDto create(@RequestBody DeanOfficeDto deanOfficeDto,
//                                @PathVariable String facultyCode) {
//        DeanOffice saved = deanOfficeServ.saveFromDto(deanOfficeDto, facultyCode);
//        return DeanOfficeMapper.toDto(saved);
//    }

    @PostMapping("/{facultyCode}")
    public DeanOffice create(@RequestBody DeanOffice deanOffice,
                             @PathVariable String facultyCode) {
        return deanOfficeServ.save(deanOffice, facultyCode);
    }


    @GetMapping
    public List<DeanOfficeDto> list() {
        return deanOfficeServ.getAll()
                .stream()
                .map(DeanOfficeMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public DeanOfficeDto byId(@PathVariable Long id) {
        return DeanOfficeMapper.toDto(deanOfficeServ.getById(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deanOfficeServ.deleteById(id);
    }
}

/*

package com.example.controller;

import com.example.dto.DeanOfficeDto;
import com.example.entity.Actors.DeanOffice;
import com.example.mapper.DeanOfficeMapper;
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
    public List<DeanOfficeDto> list() {
        return deanOfficeServ.getAll()
                .stream()
                .map(DeanOfficeMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public DeanOffice byId(@PathVariable Long id) {
        return deanOfficeServ.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deanOfficeServ.deleteById(id);
    }
}*/
