package com.example.controller;

import com.example.dto.DeanOfficeDto;
import com.example.dto.ExamDto;
import com.example.dto.FacultyCourseOfferingsDto;
import com.example.entity.Actors.DeanOffice;
import com.example.mapper.DeanOfficeMapper;
import com.example.service.DeanOfficeServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dean-offices")
@RequiredArgsConstructor
public class DeanOfficeController {

    private final DeanOfficeServ deanOfficeServ;
    private final DeanOfficeMapper deanOfficeMapper;

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
    public List<DeanOfficeDto> list(){
        return deanOfficeServ.getAll();
    }

    @GetMapping("/{id}")
    public DeanOfficeDto byId(@PathVariable Long id) {
        return deanOfficeMapper.toDto(deanOfficeServ.getById(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deanOfficeServ.deleteById(id);
    }

    @GetMapping("/{facultyCode}/getCourses")
    public ResponseEntity<FacultyCourseOfferingsDto> getCourses(
            @PathVariable String facultyCode) {

        FacultyCourseOfferingsDto dto =
                deanOfficeServ.getFacultyCourseData(facultyCode);

        return ResponseEntity.ok(dto);
    }

//    @GetMapping("/{facultyCode}/getCoursesBySemester")
//    public ResponseEntity<FacultyCourseOfferingsDto> getCoursesBySemester(
//            @PathVariable String facultyCode,
//            @RequestParam int semester) {
//
//    }

    // GET /api/v1/dean-offices/{facultyCode}/exams
    @GetMapping("/{facultyCode}/exams")
    public ResponseEntity<List<ExamDto>> getAllExamsForFaculty(
            @PathVariable String facultyCode
    ) {
        List<ExamDto> exams = deanOfficeServ.getAllExamsForFaculty(facultyCode);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{facultyCode}/exams/{examId}")
    public ResponseEntity<ExamDto> getExamDetail(
            @PathVariable String facultyCode,
            @PathVariable Integer examId
    ) {
        // (optionally verify that exam belongs to facultyCode)
        ExamDto dto = deanOfficeServ.getExamDetails(examId);
        return ResponseEntity.ok(dto);
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
