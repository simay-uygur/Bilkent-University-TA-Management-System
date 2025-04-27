package com.example.service;

import com.example.ExcelHelpers.FailedRowInfo;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.ProctorType;
import com.example.entity.General.Student;
import com.example.entity.Actors.TA;
import com.example.exception.StudentNotFoundExc;
import com.example.repo.StudentRepo;
import com.example.repo.TARepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;


@Service
@RequiredArgsConstructor
public class StudentServImpl implements StudentServ {

    private final StudentRepo studentRepo;


    private final TARepo taRepo;

    List<TA> successfulTAs = new ArrayList<>();

    public void saveAll(List<Student> students) {
        studentRepo.saveAll(students);
    }

    public Map<String, Object> importStudentsFromExcel(MultipartFile file) {
        List<Student> successfulStudents = new ArrayList<>();
        List<FailedRowInfo> failedRows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    long id = (long) row.getCell(0).getNumericCellValue();
                    String academicStatus = row.getCell(1).getStringCellValue().trim().toUpperCase();
                    String name = row.getCell(2).getStringCellValue().trim();
                    String surname = row.getCell(3).getStringCellValue().trim();
                    String department = row.getCell(4).getStringCellValue().trim();
                    int isActiveFlag = (int) row.getCell(5).getNumericCellValue();
                    int isTAFlag = (int) row.getCell(6).getNumericCellValue();
                    ProctorType proctorType = null;
                    if (isTAFlag == 1) {
                        if (row.getCell(7) != null && row.getCell(7).getCellType() == NUMERIC) { //hope works -for null column
                            int proctorTypeFlag = (int) row.getCell(7).getNumericCellValue();
                            proctorType = switch (proctorTypeFlag) {
                                case 0 -> ProctorType.NO_COURSE;
                                case 1 -> ProctorType.ALL_COURSES;
                                case 2 -> ProctorType.ONLY_ASSISTED_COURSES;
                                default -> null;
                            };
                        }
                    }

                    boolean isActive = (isActiveFlag == 1);

                    if (isTAFlag == 1) {
                        //if in the database TA with this id exists, update it's isActive flag'
                        Optional<TA> optionalTA = taRepo.findTAByTAId(id);
                        if (optionalTA.isPresent()) {
                            TA existingTA = optionalTA.get();
                            if (existingTA.getIsActive() != isActive) {
                                existingTA.setIsActive(isActive);
                                successfulTAs.add(existingTA); // Will be updated - active of the TA will be changed
                            }
                            continue; // Skip creating new TA because it already exists
                        }

                        TA ta = new TA();
                        ta.setId(id);
                        ta.setAcademic_level(AcademicLevelType.valueOf(academicStatus));
                        ta.setName(name);
                        ta.setSurname(surname);
                        ta.setDepartment(department);
                        ta.setIsActive(isActive);
                        ta.setProctorType(proctorType);

                        successfulTAs.add(ta);
                    } else {
                        // if in the database Student with this id exists, update it's isActive flag'
                        Optional<Student> optionalStudent = studentRepo.findStudentByStudentId(id);
                        if (optionalStudent.isPresent()) {
                            Student existingStudent = optionalStudent.get();
                            if (existingStudent.getIsActive() != isActive) {
                                existingStudent.setIsActive(isActive);
                                successfulStudents.add(existingStudent); // Will be updated
                            }
                            continue; // Skip creating new Student because it already exists
                        }

                        Student student = new Student();
                        student.setStudentId(id);
                        student.setAcademicStatus(academicStatus);
                        student.setStudentName(name);
                        student.setStudentSurname(surname);
                        student.setDepartment(department);
                        student.setIsActive(isActive);

                        successfulStudents.add(student);
                    }

                } catch (Exception e) {
                    StringBuilder rawData = new StringBuilder();
                    row.forEach(cell -> rawData.append(cell.toString()).append(" | "));
                    failedRows.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage(),
                            rawData.toString()
                    ));
                }
            }
        } catch (IOException e) {
            String errorMessage = "Failed to read the Excel file: " + e.getMessage();
            Map<String, Object> result = new HashMap<>();
            result.put("successStudentCount", 0);
            result.put("successTACount", 0);
            result.put("failedCount", 1);
            result.put("failedRows", List.of(new FailedRowInfo(-1, "IOException: " + e.getMessage(), "N/A")));
            return result;
        }

        if (!successfulStudents.isEmpty()) {
            studentRepo.saveAll(successfulStudents);
        }
        if (!successfulTAs.isEmpty()) {
            taRepo.saveAll(successfulTAs);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successStudentCount", successfulStudents.size());
        result.put("successTACount", successfulTAs.size());
        result.put("failedCount", failedRows.size());
        result.put("failedRows", failedRows);
        return result;
    }

    // Get a student by ID
    public Student getStudentById(Long id) {
        return studentRepo.findStudentByStudentId(id)
                .orElseThrow(() -> new StudentNotFoundExc(id));
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    // Delete a student by ID
    public void deleteStudentById(Long id) {
        Student student = studentRepo.findStudentByStudentId(id)
                .orElseThrow(() -> new StudentNotFoundExc(id));
        studentRepo.delete(student);
    }

    public Student saveStudent(Student student) {
        return studentRepo.save(student);
    }

    public Student updateStudent(Long id, Student updatedStudent) {
        Student existingStudent = studentRepo.findStudentByStudentId(id)
                .orElseThrow(() -> new StudentNotFoundExc(id));

        existingStudent.setStudentName(updatedStudent.getStudentName());
        existingStudent.setStudentSurname(updatedStudent.getStudentSurname());

        return studentRepo.save(existingStudent);
    }
}

/*
package com.example.service;

import com.example.ExcelHelpers.FailedRowInfo;
import com.example.entity.General.ProctorType;
import com.example.entity.General.Student;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface StudentServ {
    void saveAll(List<Student> students);

    Map<String, Object> importStudentsFromExcel(MultipartFile file);

    Student getStudentById(Long id);

    List<Student> getAllStudents();

    void deleteStudentById(Long id);

    Student saveStudent(Student student);

    Student updateStudent(Long id, Student updatedStudent);
}

package com.example.service;

import com.example.ExcelHelpers.FailedRowInfo;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.ProctorType;
import com.example.entity.General.Student;
import com.example.entity.Actors.TA;
import com.example.exception.StudentNotFoundExc;
import com.example.repo.StudentRepo;
import com.example.repo.TARepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

@Service
public class StudentServImpl implements StudentServ {

    private final StudentRepo studentRepo;

    private final TARepo taRepo;

    public StudentServImpl(StudentRepo studentRepo, TARepo taRepo) {
        this.studentRepo = studentRepo;
        this.taRepo = taRepo;
    }

    List<TA> successfulTAs = new ArrayList<>();

    public void saveAll(List<Student> students) {
        studentRepo.saveAll(students);
    }

    public Map<String, Object> importStudentsFromExcel(MultipartFile file) {
        List<Student> successfulStudents = new ArrayList<>();
        List<FailedRowInfo> failedRows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    long id = (long) row.getCell(0).getNumericCellValue();
                    String academicStatus = row.getCell(1).getStringCellValue().trim().toUpperCase();
                    String name = row.getCell(2).getStringCellValue().trim();
                    String surname = row.getCell(3).getStringCellValue().trim();
                    String department = row.getCell(4).getStringCellValue().trim();
                    int isActiveFlag = (int) row.getCell(5).getNumericCellValue();
                    int isTAFlag = (int) row.getCell(6).getNumericCellValue();
                    ProctorType proctorType = null;
                    if (isTAFlag == 1) {
                        if (row.getCell(7) != null && row.getCell(7).getCellType() == NUMERIC) { //hope works -for null column
                            int proctorTypeFlag = (int) row.getCell(7).getNumericCellValue();
                            proctorType = switch (proctorTypeFlag) {
                                case 0 -> ProctorType.NO_COURSE;
                                case 1 -> ProctorType.ALL_COURSES;
                                case 2 -> ProctorType.ONLY_ASSISTED_COURSES;
                                default -> null;
                            };
                        }
                    }

                    boolean isActive = (isActiveFlag == 1);

                    if (isTAFlag == 1) {
                        //if in the database TA with this id exists, update it's isActive flag'
                        Optional<TA> optionalTA = taRepo.findTAByTAId(id);
                        if (optionalTA.isPresent()) {
                            TA existingTA = optionalTA.get();
                            if (existingTA.getIsActive() != isActive) {
                                existingTA.setIsActive(isActive);
                                successfulTAs.add(existingTA); // Will be updated - active of the TA will be changed
                            }
                            continue; // Skip creating new TA because it already exists
                        }

                        TA ta = new TA();
                        ta.setId(id);
                        ta.setAcademic_level(AcademicLevelType.valueOf(academicStatus));
                        ta.setName(name);
                        ta.setSurname(surname);
                        ta.setDepartment(department);
                        ta.setIsActive(isActive);
                        ta.setProctorType(proctorType);

                        successfulTAs.add(ta);
                    } else {
                        // if in the database Student with this id exists, update it's isActive flag'
                        Optional<Student> optionalStudent = studentRepo.findStudentByStudentId(id);
                        if (optionalStudent.isPresent()) {
                            Student existingStudent = optionalStudent.get();
                            if (existingStudent.getIsActive() != isActive) {
                                existingStudent.setIsActive(isActive);
                                successfulStudents.add(existingStudent); // Will be updated
                            }
                            continue; // Skip creating new Student because it already exists
                        }

                        Student student = new Student();
                        student.setStudentId(id);
                        student.setAcademicStatus(academicStatus);
                        student.setStudentName(name);
                        student.setStudentSurname(surname);
                        student.setDepartment(department);
                        student.setIsActive(isActive);

                        successfulStudents.add(student);
                    }

                } catch (Exception e) {
                    StringBuilder rawData = new StringBuilder();
                    row.forEach(cell -> rawData.append(cell.toString()).append(" | "));
                    failedRows.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage(),
                            rawData.toString()
                    ));
                }
            }
        } catch (IOException e) {
            String errorMessage = "Failed to read the Excel file: " + e.getMessage();
            Map<String, Object> result = new HashMap<>();
            result.put("successStudentCount", 0);
            result.put("successTACount", 0);
            result.put("failedCount", 1);
            result.put("failedRows", List.of(new FailedRowInfo(-1, "IOException: " + e.getMessage(), "N/A")));
            return result;
        }

        if (!successfulStudents.isEmpty()) {
            studentRepo.saveAll(successfulStudents);
        }
        if (!successfulTAs.isEmpty()) {
            taRepo.saveAll(successfulTAs);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successStudentCount", successfulStudents.size());
        result.put("successTACount", successfulTAs.size());
        result.put("failedCount", failedRows.size());
        result.put("failedRows", failedRows);
        return result;
    }

    // Get a student by ID
    public Student getStudentById(Long id) {
        return studentRepo.findStudentByStudentId(id)
                .orElseThrow(() -> new StudentNotFoundExc(id));
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    // Delete a student by ID
    public void deleteStudentById(Long id) {
        Student student = studentRepo.findStudentByStudentId(id)
                .orElseThrow(() -> new StudentNotFoundExc(id));
        studentRepo.delete(student);
    }

    public Student saveStudent(Student student) {
        return studentRepo.save(student);
    }

    public Student updateStudent(Long id, Student updatedStudent) {
        Student existingStudent = studentRepo.findStudentByStudentId(id)
                .orElseThrow(() -> new StudentNotFoundExc(id));

        existingStudent.setStudentName(updatedStudent.getStudentName());
        existingStudent.setStudentSurname(updatedStudent.getStudentSurname());

        return studentRepo.save(existingStudent);
    }
}*/
