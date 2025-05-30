package com.example.service;

import com.example.ExcelHelpers.FailedRowInfo;
import com.example.entity.Actors.DepartmentStaff;
import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.Role;
import com.example.entity.Courses.Department;
import com.example.repo.DepartmentRepo;
import com.example.repo.DepartmentStaffRepo;
import com.example.repo.InstructorRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UploadService { //can have separate interface

    private final InstructorRepo instructorRepo;
    private final DepartmentStaffRepo departmentStaffRepo;
    private final DepartmentRepo departmentRepo;
    private final PasswordEncoder encoder;
    private final LogService log;
    public Map<String, Object> importInstructorsAndStaffFromExcel(MultipartFile file) throws IOException {
        List<Instructor> successfulInstructors = new ArrayList<>();
        List<DepartmentStaff> successfulStaff = new ArrayList<>();
        List<FailedRowInfo> failedRows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    long staffId = (long) row.getCell(0).getNumericCellValue();
                    String firstName = row.getCell(1).getStringCellValue().trim();
                    String lastName = row.getCell(2).getStringCellValue().trim();
                    String email = row.getCell(3).getStringCellValue().trim();
                    String departmentCode = row.getCell(4).getStringCellValue().trim();
                    int activeFlag = (int) row.getCell(5).getNumericCellValue();
                    int staffFlag = (int) row.getCell(6).getNumericCellValue();  // 1 = staff, 0 = instructor

                    // Read isInFaculty only if instructor
                    Cell facultyCell = row.getCell(7);
                    final Boolean isInFaculty = facultyCell != null && staffFlag == 0 ?
                            (int) facultyCell.getNumericCellValue() == 1 : null;

                    boolean isActive = (activeFlag == 1);

                    Department department = departmentRepo.findDepartmentByName(departmentCode)
                            .orElseThrow(() -> new RuntimeException("Department not found: " + departmentCode));

                    if (staffFlag == 1) {
                        Optional<DepartmentStaff> optionalStaff = departmentStaffRepo.findById(staffId);

                        DepartmentStaff staff = optionalStaff.map(existing -> {
                            existing.setName(firstName);
                            existing.setSurname(lastName);
                            existing.setWebmail(email);
                            existing.setIsActive(isActive);
                            existing.setDeleted(false);
                            existing.setDepartment(department);
                            return existing;
                        }).orElseGet(() -> {
                            DepartmentStaff newStaff = new DepartmentStaff();
                            newStaff.setId(staffId);
                            newStaff.setName(firstName);
                            newStaff.setSurname(lastName);
                            newStaff.setWebmail(email);
                            newStaff.setIsActive(isActive);
                            newStaff.setDeleted(false);
                            newStaff.setRole(Role.DEPARTMENT_STAFF);
                            newStaff.setPassword(encoder.encode("default123"));
                            newStaff.setDepartment(department);
                            return newStaff;
                        });

                        successfulStaff.add(staff);
                    } else {
                        Optional<Instructor> optionalInstructor = instructorRepo.findById(staffId);

                        Instructor instructor = optionalInstructor.map(existing -> {
                            existing.setName(firstName);
                            existing.setSurname(lastName);
                            existing.setWebmail(email);
                            existing.setIsActive(isActive);
                            existing.setDeleted(false);
                            existing.setDepartment(department);
                            existing.setIsInFaculty(isInFaculty);
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
                            newInstructor.setIsInFaculty(isInFaculty);
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

        if (!successfulStaff.isEmpty()) {
            departmentStaffRepo.saveAll(successfulStaff);
            departmentStaffRepo.flush();
        }
        log.info("Instructors and Department Staff Bulk Upload", "");
        Map<String, Object> result = new HashMap<>();
        result.put("instructorsImported", successfulInstructors.size());
        result.put("departmentStaffImported", successfulStaff.size());
        result.put("failedCount", failedRows.size());
        result.put("failedRows", failedRows);
        return result;
    }
  /*  public Map<String, Object> importInstructorsAndStaffFromExcel(MultipartFile file) throws IOException {
        List<Instructor> successfulInstructors = new ArrayList<>();
        List<DepartmentStaff> successfulStaff = new ArrayList<>();
        List<FailedRowInfo> failedRows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    long staffId = (long) row.getCell(0).getNumericCellValue();
                    String firstName = row.getCell(1).getStringCellValue().trim();
                    String lastName = row.getCell(2).getStringCellValue().trim();
                    String email = row.getCell(3).getStringCellValue().trim();
                    String departmentCode = row.getCell(4).getStringCellValue().trim();
                    int inFacultyFlag = (int) row.getCell(5).getNumericCellValue();
                    int inactiveFlag = (int) row.getCell(6).getNumericCellValue();
                    boolean isActive = (inactiveFlag == 0);

                    Department department = departmentRepo.findDepartmentByName(departmentCode)
                            .orElseThrow(() -> new RuntimeException("Department not found: " + departmentCode));

                    if (inFacultyFlag == 1) {
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
                    } else {
                        Optional<DepartmentStaff> optionalStaff = departmentStaffRepo.findById(staffId);

                        DepartmentStaff staff = optionalStaff.map(existing -> {
                            existing.setName(firstName);
                            existing.setSurname(lastName);
                            existing.setWebmail(email);
                            existing.setIsActive(isActive);
                            existing.setDeleted(false);
                            existing.setDepartment(department);
                            return existing;
                        }).orElseGet(() -> {
                            DepartmentStaff newStaff = new DepartmentStaff();
                            newStaff.setId(staffId);
                            newStaff.setName(firstName);
                            newStaff.setSurname(lastName);
                            newStaff.setWebmail(email);
                            newStaff.setIsActive(isActive);
                            newStaff.setDeleted(false);
                            newStaff.setRole(Role.DEPARTMENT_STAFF);
                            newStaff.setPassword(encoder.encode("default123"));
                            newStaff.setDepartment(department);
                            return newStaff;
                        });

                        successfulStaff.add(staff);
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

        if (!successfulStaff.isEmpty()) {
            departmentStaffRepo.saveAll(successfulStaff);
            departmentStaffRepo.flush();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("instructorsImported", successfulInstructors.size());
        result.put("departmentStaffImported", successfulStaff.size());
        result.put("failedCount", failedRows.size());
        result.put("failedRows", failedRows);
        return result;
    }*/
}