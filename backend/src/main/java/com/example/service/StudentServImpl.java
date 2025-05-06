
package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.FailedRowInfo;
import com.example.entity.Actors.Role;
import com.example.entity.Actors.TA;
import com.example.entity.Actors.TAType;
import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.ProctorType;
import com.example.entity.General.Student;
import com.example.exception.StudentNotFoundExc;
import com.example.repo.StudentRepo;
import com.example.repo.TARepo;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class StudentServImpl implements com.example.service.StudentServ {

    //should i make those autowired?
    private final StudentRepo studentRepo;

    private final TARepo taRepo;

    private final BCryptPasswordEncoder encoder; //for now it is autowired

    public void saveAll(List<Student> students) {
        studentRepo.saveAll(students);
    }


    /*
    order of the excel rows
    [0] id (numeric)
    [1] academicStatus (string)
    [2] name (string)
    [3] surname (string)
    [4] mail (string)
    [5] department (string)
    [6] isGraduatedFlag (0 = not graduated, 1 = graduated)
    [7] isTAFlag (0 or 1)
    [8] proctorTypeFlag (optional, only if TA, numeric 0/1/2)
    [9] taType (optional, only if TA, "PARTTIME" or "FULLTIME")
      */

//
//    @Transactional(readOnly = false, rollbackFor = Exception.class)
//    public Map<String, Object> importStudentsFromExcel(MultipartFile file) {
//        List<Student> successfulStudents = new ArrayList<>();
//        List<TA> successfulTAs = new ArrayList<>();
//        List<FailedRowInfo> failedRows = new ArrayList<>();
//
//        try (InputStream inputStream = file.getInputStream();
//             Workbook workbook = WorkbookFactory.create(inputStream)) {
//
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue;
//
//                try {
//                    long id = (long) row.getCell(0).getNumericCellValue();
//                    String academicStatus = row.getCell(1).getStringCellValue().trim().toUpperCase();
//                    String name = row.getCell(2).getStringCellValue().trim();
//                    String surname = row.getCell(3).getStringCellValue().trim();
//                    String mail = row.getCell(4).getStringCellValue().trim();
//
//                    if (mail.isBlank()) {
//                        mail = autoGenerateWebmail(name, surname, academicStatus);
//                    } else if (!isValidWebmail(mail, academicStatus)) {
//                        failedRows.add(new FailedRowInfo(row.getRowNum(),
//                                "Invalid webmail format for academic status."));
//                        continue;
//                    }
//
//                    String department = row.getCell(5).getStringCellValue().trim();
//
//                    // repurposed: read graduation flag
//                    int isGraduatedFlag = (row.getCell(6) != null
//                            && row.getCell(6).getCellType() == NUMERIC)
//                            ? (int) row.getCell(6).getNumericCellValue()
//                            : 0;
//                    boolean isGraduated = (isGraduatedFlag == 1);
//
//                    int isTAFlag = (int) row.getCell(7).getNumericCellValue();
//
//                    ProctorType proctorType = null;
//                    if (isTAFlag == 1 && row.getCell(8) != null
//                            && row.getCell(8).getCellType() == NUMERIC) {
//                        int proctorTypeFlag = (int) row.getCell(8).getNumericCellValue();
//                        proctorType = switch (proctorTypeFlag) {
//                            case 0 -> ProctorType.NO_COURSE;
//                            case 1 -> ProctorType.ALL_COURSES;
//                            case 2 -> ProctorType.ONLY_ASSISTED_COURSES;
//                            default -> null;
//                        };
//                    }
//
//                    String taType = null;
//                    if (isTAFlag == 1 && row.getCell(9) != null
//                            && row.getCell(9).getCellType() == STRING) {
//                        taType = row.getCell(9).getStringCellValue().trim().toUpperCase();
//                        if (!taType.equals("PARTTIME") && !taType.equals("FULLTIME")) {
//                            failedRows.add(new FailedRowInfo(row.getRowNum(),
//                                    "Invalid TA Type. Must be PARTTIME or FULLTIME."));
//                            continue;
//                        }
//                    }
//
//                    // always active
//                    boolean isActive = true;
//
//                    if (isTAFlag == 1) {
//                        Optional<TA> optionalTA = taRepo.findById(id);
//                        if (optionalTA.isPresent()) {
//                            TA existingTA = optionalTA.get();
//                            boolean changed = false;
//
//                            // ensure active
//                            if (!existingTA.getIsActive()) {
//                                existingTA.setIsActive(true);
//                                changed = true;
//                            }
//                            // update graduation status
//                            if (!Objects.equals(existingTA.getIsGraduated(), isGraduated)) {
//                                existingTA.setIsGraduated(isGraduated);
//                                changed = true;
//                            }
//                            if (changed) {
//                                successfulTAs.add(existingTA);
//                            }
//                            continue;
//                        }
//
//                        // create new TA
//                        TA newTa = new TA();
//                        newTa.setId(id);
//                        newTa.setName(name);
//                        newTa.setSurname(surname);
//                        newTa.setWebmail(mail);
//                        newTa.setAcademic_level(AcademicLevelType.valueOf(academicStatus));
//                        newTa.setDepartment(department);
//                        newTa.setIsActive(true);
//                        newTa.setIsGraduated(isGraduated);
//                        newTa.setRole(Role.TA);
//                        newTa.setTa_type(TAType.valueOf(taType));
//                        newTa.setProctorType(proctorType);
//                        newTa.setPassword(encoder.encode("default123"));
//                        newTa.setTotal_workload(0);
//
//                        successfulTAs.add(newTa);
//
//                    } else {
//                        Optional<Student> optionalStudent = studentRepo.findStudentByStudentId(id);
//                        if (optionalStudent.isPresent()) {
//                            Student existingStudent = optionalStudent.get();
//                            if (!existingStudent.getIsActive()) {
//                                existingStudent.setIsActive(true);
//                                successfulStudents.add(existingStudent);
//                            }
//                            continue;
//                        }
//
//                        Student student = new Student();
//                        student.setStudentId(id);
//                        student.setAcademicStatus(academicStatus);
//                        student.setStudentName(name);
//                        student.setStudentSurname(surname);
//                        student.setWebmail(mail);
//                        student.setDepartment(department);
//                        student.setIsActive(true);
//
//                        successfulStudents.add(student);
//                    }
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
//        } catch (IOException e) {
//            failedRows.add(new FailedRowInfo(-1, "IOException: " + e.getMessage()));
//        }
//
//        if (!successfulStudents.isEmpty()) {
//            studentRepo.saveAll(successfulStudents);
//        }
//        if (!successfulTAs.isEmpty()) {
//            taRepo.saveAll(successfulTAs);
//        }
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("successStudentCount", successfulStudents.size());
//        result.put("successTACount", successfulTAs.size());
//        result.put("failedCount", failedRows.size());
//        result.put("failedRows", failedRows);
//        return result;
//    }
//


    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public Map<String, Object> importStudentsFromExcel(MultipartFile file) {
        List<Student> successfulStudents = new ArrayList<>();
        List<TA> successfulTAs      = new ArrayList<>();
        List<FailedRowInfo> failedRows = new ArrayList<>();

    /*
    order of the excel rows
    [0] id (numeric)
    [1] academicStatus (string)
    [2] name (string)
    [3] surname (string)
    [4] mail (string)
    [5] department (string)
    [6] isGraduatedFlag (0 = not graduated, 1 = graduated)
    [7] isTAFlag (0 or 1)
    [8] proctorTypeFlag (optional, only if TA, numeric 0/1/2)
    [9] taType (optional, only if TA, "PARTTIME" or "FULLTIME")
    */

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook      = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    // --- safe cell retrieval ---
                    Cell idCell         = row.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell statusCell     = row.getCell(1, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell nameCell       = row.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell surnameCell    = row.getCell(3, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell mailCell       = row.getCell(4, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell deptCell       = row.getCell(5, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell gradFlagCell   = row.getCell(6, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell taFlagCell     = row.getCell(7, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell proctorFlagCell= row.getCell(8, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell taTypeCell     = row.getCell(9, MissingCellPolicy.CREATE_NULL_AS_BLANK);

                    long   id             = (long) idCell.getNumericCellValue();
                    String academicStatus = statusCell.getStringCellValue().trim().toUpperCase();
                    String name           = nameCell.getStringCellValue().trim();
                    String surname        = surnameCell.getStringCellValue().trim();
                    String mail           = mailCell.getStringCellValue().trim();
                    String department     = deptCell.getStringCellValue().trim();

                    if (mail.isBlank()) {
                        mail = autoGenerateWebmail(name, surname, academicStatus);
                    } else if (!isValidWebmail(mail, academicStatus)) {
                        failedRows.add(new FailedRowInfo(row.getRowNum(),
                                "Invalid webmail format for academic status."));
                        continue;
                    }

                    boolean isGraduated = ((int) gradFlagCell.getNumericCellValue()) == 1;
                    int     isTAFlag    = (int) taFlagCell.getNumericCellValue();

                    ProctorType proctorType = null;
                    if (isTAFlag == 1 && proctorFlagCell.getCellType() == NUMERIC) {
                        int pf = (int) proctorFlagCell.getNumericCellValue();
                        proctorType = switch (pf) {
                            case 0 -> ProctorType.NO_COURSE;
                            case 1 -> ProctorType.ALL_COURSES;
                            case 2 -> ProctorType.ONLY_ASSISTED_COURSES;
                            default -> null;
                        };
                    }

                    String taType = "";
                    if (isTAFlag == 1 && taTypeCell.getCellType() == STRING) {
                        taType = taTypeCell.getStringCellValue().trim().toUpperCase();
                        if (!taType.equals("PARTTIME") && !taType.equals("FULLTIME")) {
                            failedRows.add(new FailedRowInfo(row.getRowNum(),
                                    "Invalid TA Type. Must be PARTTIME or FULLTIME."));
                            continue;
                        }
                    }

                    // always true now
                    boolean isActive = true;

                    if (isTAFlag == 1) {
                        Optional<TA> opt = taRepo.findById(id);
                        if (opt.isPresent()) {
                            TA existing = opt.get();
                            boolean changed = false;
                            if (!existing.getIsActive()) {
                                existing.setIsActive(true);
                                changed = true;
                            }
                            if (!Objects.equals(existing.getIsGraduated(), isGraduated)) {
                                existing.setIsGraduated(isGraduated);
                                changed = true;
                            }
                            if (changed) successfulTAs.add(existing);
                            continue;
                        }

                        // create new TA
                        TA newTa = new TA();
                        newTa.setId(id);
                        newTa.setName(name);
                        newTa.setSurname(surname);
                        newTa.setWebmail(mail);
                        newTa.setAcademicLevel(AcademicLevelType.valueOf(academicStatus));
                        newTa.setDepartment(department);
                        newTa.setIsActive(true);
                        newTa.setIsGraduated(isGraduated);
                        newTa.setRole(Role.TA);
                        newTa.setTaType(TAType.valueOf(taType));
                        newTa.setProctorType(proctorType);
                        newTa.setPassword(encoder.encode("default123"));
                        newTa.setTotalWorkload(0);
                        successfulTAs.add(newTa);

                    } else {
                        Optional<Student> optStu = studentRepo.findStudentByStudentId(id);
                        if (optStu.isPresent()) {
                            Student existing = optStu.get();
                            if (!existing.getIsActive()) {
                                existing.setIsActive(true);
                                successfulStudents.add(existing);
                            }
                            continue;
                        }
                        Student stu = new Student();
                        stu.setStudentId(id);
                        stu.setAcademicStatus(academicStatus);
                        stu.setStudentName(name);
                        stu.setStudentSurname(surname);
                        stu.setWebmail(mail);
                        stu.setDepartment(department);
                        stu.setIsActive(true);
                        successfulStudents.add(stu);
                    }

                } catch (Exception e) {
                    StringBuilder raw = new StringBuilder();
                    row.forEach(c -> raw.append(c.toString()).append(" | "));
                    failedRows.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }

        } catch (IOException e) {
            failedRows.add(new FailedRowInfo(-1, "IOException: " + e.getMessage()));
        }

        if (!successfulStudents.isEmpty()) studentRepo.saveAll(successfulStudents);
        if (!successfulTAs.isEmpty())      taRepo.saveAll(successfulTAs);

        Map<String, Object> result = new HashMap<>();
        result.put("successStudentCount", successfulStudents.size());
        result.put("successTACount",      successfulTAs.size());
        result.put("failedCount",         failedRows.size());
        result.put("failedRows",          failedRows);
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


    public boolean isValidWebmail(String webmail, String academicStatus) {
        if (webmail == null || academicStatus == null) {
            return false;
        }
        webmail = webmail.trim().toLowerCase();
        academicStatus = academicStatus.trim().toUpperCase();

        if (academicStatus.equals("BS")) {
            return webmail.endsWith("@ug.bilkent.edu.tr");
        } else if (academicStatus.equals("MS") || academicStatus.equals("PHD")) {
            return webmail.endsWith("@bilkent.edu.tr") && !webmail.contains("@ug.bilkent.edu.tr");
        }
        return false; // unknown academic status
    }

    //if it generates an email that exists, what will happen?
    public String autoGenerateWebmail(String fullName, String surname, String academicStatus) {
        if (fullName == null || surname == null || academicStatus == null) {
            throw new IllegalArgumentException("Name, surname, and academic status must not be null");
        }

        String[] parts = fullName.trim().split("\\s+");
        String mailPrefix;

        if (parts.length == 1) {
            mailPrefix = parts[0].toLowerCase() + "." + surname.toLowerCase();
        } else if (parts.length == 2) {
            mailPrefix = parts[0].toLowerCase() + "." + surname.toLowerCase();
        } else { // 3 or more names
            mailPrefix = parts[1].toLowerCase() + "." + surname.toLowerCase();
        }

        academicStatus = academicStatus.trim().toUpperCase();
        String domain;
        if (academicStatus.equals("BS")) {
            domain = "@ug.bilkent.edu.tr";
        } else {
            domain = "@bilkent.edu.tr";
        }

        return mailPrefix + domain;
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
