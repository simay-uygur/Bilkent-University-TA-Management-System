package com.example.service;


import com.example.dto.ClassRoomDto;
import com.example.dto.FailedRowInfo;
import com.example.entity.General.ClassRoom;
import com.example.entity.General.Event;
import com.example.exception.GeneralExc;
import com.example.repo.ClassRoomRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.entity.General.Event;

@Service
@RequiredArgsConstructor
public class ClassRoomServImpl implements ClassRoomServ {

    private final ClassRoomRepo classRoomRepo;

    @Override
    public Map<String, Object> importClassRoomsFromExcel(MultipartFile file) throws IOException {
        List<ClassRoom> successful = new ArrayList<>();
        List<FailedRowInfo> failed = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                try {
                    String id     = row.getCell(0).getStringCellValue().trim().toUpperCase();
                    int capacity  = (int) row.getCell(1).getNumericCellValue();
                    int examCap   = (int) row.getCell(2).getNumericCellValue();

                    if (classRoomRepo.existsById(id)) {
                        throw new IllegalArgumentException("Classroom already exists: " + id);
                    }

                    ClassRoom room = new ClassRoom();
                    room.setClassroomId(id);
                    room.setClassCapacity(capacity);
                    room.setExamCapacity(examCap);
                    room.setExamRooms(new ArrayList<>()); // empty relationship

                    successful.add(room);

                } catch (Exception e) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }
        }

        if (!successful.isEmpty()) {
            classRoomRepo.saveAll(successful);
            classRoomRepo.flush();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successful.size());
        result.put("failedCount", failed.size());
        result.put("failedRows", failed);
        return result;
    }

    private ClassRoomDto toDto(ClassRoom c) {
        return new ClassRoomDto(c.getClassroomId(), c.getClassCapacity(), c.getExamCapacity());
    }

    private ClassRoom toEntity(ClassRoomDto dto) {
        ClassRoom c = new ClassRoom();
        c.setClassroomId(dto.getClassroomId().toUpperCase());
        c.setClassCapacity(dto.getClassCapacity());
        c.setExamCapacity(dto.getExamCapacity());
        return c;
    }

    @Override
    public ClassRoomDto create(ClassRoomDto dto) {
        if (classRoomRepo.existsById(dto.getClassroomId())) {
            throw new IllegalArgumentException("Classroom with this ID already exists");
        }
        return toDto(classRoomRepo.save(toEntity(dto)));
    }

    @Override
    public ClassRoomDto update(String id, ClassRoomDto dto) {
        ClassRoom existing = classRoomRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
        existing.setClassCapacity(dto.getClassCapacity());
        existing.setExamCapacity(dto.getExamCapacity());
        return toDto(classRoomRepo.save(existing));
    }

    @Override
    public void delete(String id) {
        if (!classRoomRepo.existsById(id)) {
            throw new IllegalArgumentException("Classroom not found");
        }
        classRoomRepo.deleteById(id);
    }

    @Override
    public ClassRoomDto getById(String id) {
        return classRoomRepo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
    }

    @Override
    public List<ClassRoomDto> getAll() {
        return classRoomRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ClassRoomDto> getClassRoomsByTime(Event duration){
        List<ClassRoomDto> availableRooms = classRoomRepo.findAll().parallelStream()
        .filter(room -> room.isFree(duration))
        .map(r -> new ClassRoomDto(
            r.getClassroomId(),
            r.getClassCapacity(),
            r.getExamCapacity()))
        .collect(Collectors.toList());
        if (availableRooms.isEmpty())
        {
            throw new GeneralExc("No classrooms are available for that time");
        }
        return availableRooms;
    }

}


