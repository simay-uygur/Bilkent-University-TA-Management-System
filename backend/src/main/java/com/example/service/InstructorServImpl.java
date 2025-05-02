package com.example.service;



import com.example.ExcelHelpers.FailedRowInfo;
import com.example.dto.InstructorDto;
import com.example.entity.Actors.Instructor;

import com.example.entity.Courses.Department;
import com.example.mapper.InstructorMapper;
import com.example.repo.DepartmentRepo;
import com.example.repo.InstructorRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.entity.Actors.Role;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorServImpl implements InstructorServ {

    private final InstructorRepo instructorRepo;
    private final PasswordEncoder encoder;
    private final DepartmentRepo departmentRepo;
    private final InstructorMapper instructorMapper;


    @Override
    public Map<String, Object> importInstructorsFromExcel(MultipartFile file) throws IOException {
        List<Instructor> successfulInstructors = new ArrayList<>();
        List<FailedRowInfo> failedRows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                try {
                    long staffId = (long) row.getCell(0).getNumericCellValue();
                    String firstName = row.getCell(1).getStringCellValue().trim();
                    String lastName = row.getCell(2).getStringCellValue().trim();
                    String email = row.getCell(3).getStringCellValue().trim();
                    String departmentCode = row.getCell(4).getStringCellValue().trim();
                    int inFacultyFlag = (int) row.getCell(5).getNumericCellValue(); // 1 = in faculty
                    int inactiveFlag = (int) row.getCell(6).getNumericCellValue(); // 0 = active, 1 = inactive
                    boolean isActive = (inactiveFlag == 0);

                    if (inFacultyFlag == 1) {
                        Department department = departmentRepo.findDepartmentByName(departmentCode)
                                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentCode));

                        Optional<Instructor> optionalInstructor = instructorRepo.findById(staffId);

                        Instructor instructor = optionalInstructor.map(existing -> {
                            existing.setName(firstName);
                            existing.setSurname(lastName);
                            existing.setWebmail(email);
                            existing.setIsActive(isActive);
                            existing.setDeleted(false);
                            existing.setDepartment(department);
                            return existing;
                        }).orElseGet(() -> {
                            Instructor newInstructor = new Instructor();
                            newInstructor.setId(staffId);
                            newInstructor.setName(firstName);
                            newInstructor.setSurname(lastName);
                            newInstructor.setWebmail(email);
                            newInstructor.setIsActive(isActive);
                            newInstructor.setDeleted(false);
                            newInstructor.setRole(Role.INSTRUCTOR);
                            newInstructor.setPassword(encoder.encode("default123"));
                            newInstructor.setDepartment(department);
                            return newInstructor;
                        });

                        successfulInstructors.add(instructor);
                    }

                } catch (Exception e) {
                    StringBuilder rawData = new StringBuilder();
                    row.forEach(cell -> rawData.append(cell.toString()).append(" | "));
                    failedRows.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }
        }

        if (!successfulInstructors.isEmpty()) {
            instructorRepo.saveAll(successfulInstructors);
            instructorRepo.flush();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successfulInstructors.size());
        result.put("failedCount", failedRows.size());
        result.put("failedRows", failedRows);
        return result;
    }
//
//    @Override
//    public Map<String, Object> importInstructorsFromExcel(MultipartFile file) throws IOException {
//        List<Instructor> successfulInstructors = new ArrayList<>();
//        List<FailedRowInfo> failedRows = new ArrayList<>();
//
//        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // Skip header row
//
//                try {
//                    long staffId = (long) row.getCell(0).getNumericCellValue();
//                    String firstName = row.getCell(1).getStringCellValue().trim();
//                    String lastName = row.getCell(2).getStringCellValue().trim();
//                    String email = row.getCell(3).getStringCellValue().trim();
//                    String departmentCode = row.getCell(4).getStringCellValue().trim();
//                    int inFacultyFlag = (int) row.getCell(5).getNumericCellValue(); // 1 = in faculty, 0 = office staff
//                    int inactiveFlag = (int) row.getCell(6).getNumericCellValue(); // 0 = active, 1 = inactive
//                    boolean isActive = (inactiveFlag == 0);
//
//                    // Only create Instructor if inFaculty = 1
//                    if (inFacultyFlag == 1) {
//                        Optional<Instructor> optionalInstructor = repo.findById(staffId); // id must be unique for each staff
//
//                        Instructor instructor = optionalInstructor.map(existing -> {
//                            existing.setName(firstName);
//                            existing.setSurname(lastName);
//                            existing.setWebmail(email);
//                            existing.setIsActive(isActive);
//                            existing.setDeleted(false); // Just in case
//                            return existing;
//                        }).orElseGet(() -> {
//                            Instructor newInstructor = new Instructor();
//                            newInstructor.setId(staffId);
//                            newInstructor.setName(firstName);
//                            newInstructor.setSurname(lastName);
//                            newInstructor.setWebmail(email);
//                            newInstructor.setIsActive(isActive);
//                            newInstructor.setRole(Role.INSTRUCTOR);
//                            newInstructor.setPassword(encoder.encode("default123"));
//                            return newInstructor;
//                        });
//
//                        successfulInstructors.add(instructor);
//                    }
//                    // else -> If inFaculty == 0, later DepartmentOffice object will be created elsewhere
//
//                } catch (Exception e) {
//                    StringBuilder rawData = new StringBuilder();
//                    row.forEach(cell -> rawData.append(cell.toString()).append(" | "));
//                    failedRows.add(new FailedRowInfo(
//                            row.getRowNum(),
//                            e.getClass().getSimpleName() + ": " + e.getMessage()
//                    ));
//                }
//            }
//        }
//
//        if (!successfulInstructors.isEmpty()) {
//            repo.saveAll(successfulInstructors);
//            repo.flush();
//        }
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("successCount", successfulInstructors.size());
//        result.put("failedCount", failedRows.size());
//        result.put("failedRows", failedRows);
//        return result;
//    }

    @Override
    public Instructor createInstructor(Instructor instructor) {
        if (instructorRepo.existsById(instructor.getId())) {
            throw new RuntimeException("Instructor with id " + instructor.getId() + " already exists.");
        }

        instructor.setRole(Role.INSTRUCTOR);

        String rawPassword = instructor.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        instructor.setPassword(encoder.encode(rawPassword));

        return instructorRepo.save(instructor);
    }

    @Override
    public boolean deleteInstructor(Long id) {
        if (!instructorRepo.existsById(id)) {
            throw new RuntimeException("Instructor with id " + id + " not found.");
        }
        instructorRepo.deleteById(id);
        return true;
    }

    @Override
    public Instructor updateInstructor(Long id, Instructor instructor) {
        Instructor existingInstructor = instructorRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Instructor with id " + id + " not found."));

        existingInstructor.setName(instructor.getName());
        existingInstructor.setSurname(instructor.getSurname());
        existingInstructor.setWebmail(instructor.getWebmail());
        existingInstructor.setIsActive(instructor.getIsActive());
        //existingInstructor.setde  -  department set

        //existingInstructor.setDeleted(instructor.g);
        // You can add other fields you want to update here

        return instructorRepo.save(existingInstructor);
    }

    @Override
    public List<Instructor> getAllInstructors() {
        return instructorRepo.findAll();
    }

    @Override
    public List<InstructorDto> getAllInstructorsDto() {
        return instructorRepo.findAll().stream()
                .map(instructorMapper::toDto)
                .collect(Collectors.toList());
    }


    /* @Override
    public List<InstructorDto> getInstructorsByDepartment(String departmentName) {
        Department dept = departmentRepo
                .findDepartmentByName(departmentName)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentName));

        return instructorRepo.findAll().stream()
                .filter(i -> dept.equals(i.getDepartment()))
                .map(instructorMapper::toDto)
                .collect(Collectors.toList());
    } */
    @Override
    public List<InstructorDto> getInstructorsByDepartment(String departmentName) {
        List<Instructor> instructors = instructorRepo.findByDepartmentName(departmentName)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentName));

      return instructors.stream()
                .map(instructorMapper::toDto)
                .collect(Collectors.toList());
    //return instructorMapper.toDtoList(instructors);
}

}