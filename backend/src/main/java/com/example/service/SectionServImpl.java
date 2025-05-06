package com.example.service;

import com.example.dto.FailedRowInfo;
import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Section;
import com.example.entity.General.Semester;
import com.example.entity.General.Student;
import com.example.entity.General.Term;
import com.example.repo.CourseRepo;
import com.example.repo.SectionRepo;
import com.example.repo.StudentRepo;
import com.example.repo.TARepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SectionServImpl implements SectionServ {

    private final SectionRepo repo;
    private final SemesterServ semesterService;
    private final CourseOfferingServ offeringService;
    private final InstructorServ instructorService;
    private final CourseRepo courseRepo;
    private final StudentServ studentService;
    private final StudentRepo studentRepo;
    private final TAServ taService;
    private final TARepo taRepo;

    private final SectionRepo sectionRepo;

    @Override
    public Section create(Section section) {
        // 1) sectionCode must be supplied in the right format
        String code = section.getSectionCode();
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "sectionCode is required (e.g. CS-319-1-2025-SPRING)");
        }
        code = code.toUpperCase();

        String[] parts = code.split("-");
        if (parts.length != 5) {
            throw new IllegalArgumentException(
                    "Invalid sectionCode format. Expected DEPT-CourseNo-SectionNo-Year-(SPRING|SUMMER|FALL); got: "
                            + code);
        }

        String deptCode = parts[0];
        int courseNo;
        int parsedSectionNo; // for validation only
        int year;
        Term term;
        try {
            courseNo        = Integer.parseInt(parts[1]);
            parsedSectionNo = Integer.parseInt(parts[2]);
            year            = Integer.parseInt(parts[3]);
            term            = Term.valueOf(parts[4]);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid numbers/term in sectionCode '" + code + "': " + e.getMessage());
        }

        Semester sem = semesterService
                .findByYearAndTerm(year, String.valueOf(term))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No semester found for " + year + " " + term));

        String fullCourseCode = deptCode + "-" + courseNo;
        Course course = courseRepo
                .findByCourseCodeIgnoreCase(fullCourseCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No course found with code " + fullCourseCode));

        CourseOffering off = offeringService
                .getByCourseAndSemester((long) course.getCourseId(), sem.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No offering for course " + fullCourseCode +
                                " in semester " + year + " " + term));

        if (section.getInstructor() == null || section.getInstructor().getId() == null) {
            throw new IllegalArgumentException("instructor.id must be provided");
        }
        Instructor instr = instructorService.getById(section.getInstructor().getId());


        section.setSectionCode(code);
        section.setOffering(off);
        section.setInstructor(instr);

        if (repo.existsBySectionCodeEqualsIgnoreCase(code)) {
            throw new IllegalArgumentException(
                    "Section with code '" + code + "' already exists.");
        }

        return repo.save(section);
    }

    @Override
    public Section update(Integer id, Section section) {
        Section existing = getById(id);
        existing.setSectionCode(section.getSectionCode());
        existing.setOffering(section.getOffering());
        // TODO: update other fields/relationships as needed
        return repo.save(existing);
    }

    //those should be fixed according to the new sectioncode format
    @Override
    public Section getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + id));
    }
    public Section getBySectionCode(String sectionCode) {
        return repo.findBySectionCodeIgnoreCase(sectionCode)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));
    }
    @Override
    public List<Section> getAll() {
        return repo.findAll();
    }

    @Override
    public void delete(Integer id) {
        repo.deleteById(id);
    }

    // this is for uploading sections with instructors (coordinator row non-existent in the excel file)
    @Override
    @Transactional
    public Map<String,Object> importFromExcel(MultipartFile file) throws IOException {
        List<Section> successful = new ArrayList<>();
        List<FailedRowInfo> failed  = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // header

                // 1) parse Term enum — if bad, record failure & skip
                String termStr = row.getCell(4).getStringCellValue().trim();
                Term termEnum;
                try {
                    termEnum = Term.valueOf(termStr.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            "InvalidTerm: must be SPRING, SUMMER, or FALL; found '" + termStr + "'"
                    ));
                    continue;  // skip to next row
                }

                try {
                    String deptCode = row.getCell(0).getStringCellValue().trim();
                    int    courseNo = (int) row.getCell(1).getNumericCellValue();
                    int    sectionNo= (int) row.getCell(2).getNumericCellValue();
                    int    year     = (int) row.getCell(3).getNumericCellValue();


                    long   staffId  = (long) row.getCell(5).getNumericCellValue();

                    // 2) semester lookup
                    Semester sem = semesterService
                            .findByYearAndTerm(year, String.valueOf(termEnum))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Semester not found: " + year + " " + termEnum));

                    // 3) course lookup
                    String fullCode = deptCode.toUpperCase() + "-" + courseNo;
                    Course course = courseRepo
                            .findByCourseCodeIgnoreCase(fullCode)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Course not found: " + fullCode));

                    // 4) offering
                    CourseOffering off = offeringService
                            .getByCourseAndSemester((long) course.getCourseId(), sem.getId())
                            .orElseGet(() -> {
                                CourseOffering newOff = new CourseOffering();
                                newOff.setCourse(course);
                                newOff.setSemester(sem);
                                return offeringService.create(newOff);
                            });

                    // 5) instructor
                    Instructor instr = instructorService.getById(staffId);

                    // 6) build & collect section

                    String sectionCode = String.format("%s-%d-%d-%s",
                            course.getCourseCode(), sectionNo, year, termEnum);

// Check for duplicates before adding to successful list
                    if (repo.existsBySectionCodeEqualsIgnoreCase(sectionCode)) {
                        throw new IllegalArgumentException(
                                "Section with code '" + sectionCode + "' already exists.");
                    }

                    Section sec = new Section();
                    sec.setSectionCode(sectionCode);
                    sec.setOffering(off);
                    sec.setInstructor(instr);
                    successful.add(sec);

                } catch (Exception e) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
                }
            }
        }

        if (!successful.isEmpty()) {
            repo.saveAll(successful);
            repo.flush();
        }

        Map<String,Object> result = new HashMap<>();
        result.put("successCount", successful.size());
        result.put("failedCount",  failed.size());
        result.put("failedRows",   failed);
        return result;
    }

    // old code
//    @Override
//    @Transactional
//    public Map<String,Object> importFromExcel(MultipartFile file) throws IOException {
//        List<Section> successful = new ArrayList<>();
//        List<FailedRowInfo> failed  = new ArrayList<>();
//
//        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
//            Sheet sheet = wb.getSheetAt(0);
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue;  // header
//
//                try {
//                    String deptCode = row.getCell(0).getStringCellValue().trim();
//                    int    courseNo = (int) row.getCell(1).getNumericCellValue();
//                    int    sectionNo= (int) row.getCell(2).getNumericCellValue();
//                    int    year     = (int) row.getCell(3).getNumericCellValue();
//                    String termStr  = row.getCell(4).getStringCellValue().trim().toUpperCase();
//                    long   staffId  = (long) row.getCell(5).getNumericCellValue();
//
//                    // 1) semester
//                    Semester sem = semesterService
//                            .findByYearAndTerm(year, termStr)
//                            .orElseThrow(() -> new IllegalArgumentException(
//                                    "Semester not found: " + year + " " + termStr));
//                    // assume deptCode = "CS", courseNo = "319"
//                    String fullCode = deptCode.toUpperCase() + "-" + courseNo;
//                    // e.g. "CS‑319"
//                    Course course = courseRepo
//                            .findByCourseCodeIgnoreCase(fullCode)
//                            .orElseThrow(() -> new IllegalArgumentException(
//                                    "Course not found: " + fullCode
//                            ));
//                    // 3) offering
//                    CourseOffering off = offeringService
//                            .getByCourseAndSemester((long) course.getCourseId(), sem.getId())
//                            .orElseGet(() -> {
//                                // build a brand‑new offering for this course + semester
//                                CourseOffering newOff = new CourseOffering();
//                                newOff.setCourse(course);
//                                newOff.setSemester(sem);
//                                // this will persist it (and because of your unique constraint
//                                // on (course,semester) it won't duplicate if another thread snuck one
//                                // in between)
//                                return offeringService.create(newOff);
//                            });
//                    // 4) instructor
//                    Instructor instr = instructorService.getById(staffId);
//
//                    // 5) build section
//
//                    String sectionCode = course.getCourseCode() + "-" + sectionNo;
//                    try {
//                        // Check if section exists - this should be in its own transaction
//                        if (repo.existsBySectionCodeEqualsIgnoreCase(sectionCode)) {
//                            failed.add(new FailedRowInfo(
//                                    row.getRowNum(),
//                                    "DuplicateKeyException: Section with code '" + sectionCode + "' already exists."
//                            ));
//                            continue; // Skip to next row
//                        }
//
//                        Section sec = new Section();
//                        sec.setSectionCode(sectionCode);
//                        sec.setOffering(off);
//                        sec.setInstructor(instr);
//
//                        // Save immediately - don't collect for batch saving
//                        Section saved = repo.save(sec);
//                        repo.flush(); // Force immediate commit
//                        successful.add(saved);
//
//                    } catch (Exception e) {
//                        // Catch duplicate key exceptions that might occur anyway
//                        failed.add(new FailedRowInfo(
//                                row.getRowNum(),
//                                "Database error: " + e.getMessage()
//                        ));
//                    }
//
//                } catch (Exception e) {
//                    failed.add(new FailedRowInfo(
//                            row.getRowNum(),
//                            e.getClass().getSimpleName() + ": " + e.getMessage()
//                    ));
//                }
//            }
//        }
//
    //// Check if section already exists
    ///* if (repo.existsBySectionCodeEqualsIgnoreCase(sectionCode)) {
//    throw new IllegalArgumentException("Section with code '" + sectionCode + "' already exists.");
//}
//
//                    Section sec = new Section();
//                    sec.setSectionCode(course.getCourseCode() + "-" + sectionNo);
//                    sec.setOffering(off);
//                    sec.setInstructor(instr);
//
//                    successful.add(sec);
//
//                } catch (Exception e) {
//                    failed.add(new FailedRowInfo(
//                            row.getRowNum(),
//                            e.getClass().getSimpleName() + ": " + e.getMessage()
//                    ));
//                }
//            }
//        }
//
//        if (!successful.isEmpty()) {
//            repo.saveAll(successful);
//            repo.flush();
//        } */
//
//        Map<String,Object> result = new HashMap<>();
//        result.put("successCount", successful.size());
//        result.put("failedCount",  failed.size());
//        result.put("failedRows",   failed);
//        return result;
//    }

    @Override
    public Map<String,Object> importSectionStudentsFromExcel(MultipartFile file) throws IOException {
        List<Section> successful = new ArrayList<>();
        List<FailedRowInfo> failed    = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // skip header

                // 0) parse & validate term first
                String termStr = getStringCellValue(row.getCell(5)).toUpperCase();
                Term term;
                try {
                    term = Term.valueOf(termStr);
                } catch (IllegalArgumentException e) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            "InvalidTerm: must be SPRING, SUMMER, or FALL; found '" + termStr + "'"
                    ));
                    continue;
                }

                try {
                    // 1) read core fields
                    long   personId  = getLongCellValue(row.getCell(0));
                    String deptCode  = getStringCellValue(row.getCell(1)).toUpperCase();
                    int    courseNo  = (int) getNumericCellValue(row.getCell(2));
                    int    sectionNo = (int) getNumericCellValue(row.getCell(3));
                    int    year      = (int) getNumericCellValue(row.getCell(4));

                    // 2) find Semester by (year, term)
                    Semester sem = semesterService
                            .findByYearAndTerm(year, String.valueOf(term))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Semester not found: " + year + " " + term));

                    // 3) find Course
                    String fullCode = deptCode + "-" + courseNo;
                    Course course = courseRepo
                            .findByCourseCodeIgnoreCase(fullCode)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Course not found: " + fullCode));

                    // 4) find-or-create Offering
                    CourseOffering off = offeringService
                            .getByCourseAndSemester((long) course.getCourseId(), sem.getId())
                            .orElseGet(() -> {
                                CourseOffering nf = new CourseOffering();
                                nf.setCourse(course);
                                nf.setSemester(sem);
                                return offeringService.create(nf);
                            });

                    // 5) build full sectionCode and lookup Section
                    String secCode = String.format(
                            "%s-%d-%d-%s",
                            fullCode, sectionNo, year, term.name()
                    );
                    Section section = repo
                            .findBySectionCodeIgnoreCase(secCode)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Section not found: " + secCode));

                    // 6) attach person
                    if (studentRepo.existsById(personId)) {
                        Student s = studentService.getStudentById(personId);
                        if (section.getRegisteredStudents().stream()
                                .noneMatch(stu -> stu.getStudentId().equals(s.getStudentId()))) {
                            section.getRegisteredStudents().add(s);
                        }
                    } else if (taRepo.existsById(personId)) {
                        TA t = taService.getTAByIdTa(personId);
                        if (section.getRegisteredTas().stream().noneMatch(ta -> ta.getId().equals(t.getId()))) {
                            section.getRegisteredTas().add(t);
                        }
                    } else {
                        throw new IllegalArgumentException(
                                "No student or TA with id: " + personId);
                    }

                    successful.add(section);

                } catch (Exception ex) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            ex.getClass().getSimpleName() + ": " + ex.getMessage()
                    ));
                }
            }
        }

        if (!successful.isEmpty()) {
            repo.saveAll(successful);
            repo.flush();
        }

        return Map.of(
                "successCount", successful.size(),
                "failedCount",  failed.size(),
                "failedRows",   failed
        );
    }

    //@Transactional
// for importing staff (instructors) with related sections and creates
//    @Override
//    public Map<String,Object> importSectionStudentsFromExcel(MultipartFile file) throws IOException {
//        List<Section> successful = new ArrayList<>();
//        List<FailedRowInfo> failed    = new ArrayList<>();
//
//        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
//            Sheet sheet = wb.getSheetAt(0);
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // skip header
//
//                try {
//                    long   personId  = getLongCellValue(row.getCell(0));
//                    String deptCode  = getStringCellValue(row.getCell(1)).toUpperCase();
//                    int    courseNo  = (int) getNumericCellValue(row.getCell(2));
//                    int    sectionNo = (int) getNumericCellValue(row.getCell(3));
//                    int    year      = (int) getNumericCellValue(row.getCell(4));
//                    String termStr   = getStringCellValue(row.getCell(5)).toUpperCase();
    ////                    long   personId  = (long) row.getCell(0).getNumericCellValue();
    ////                    String deptCode  = row.getCell(1).getStringCellValue().trim().toUpperCase();
    ////                    int    courseNo  = (int) row.getCell(2).getNumericCellValue();
    ////                    int    sectionNo = (int) row.getCell(3).getNumericCellValue();
    ////                    int    year      = (int) row.getCell(4).getNumericCellValue();
    ////                    String termStr   = row.getCell(5).getStringCellValue().trim().toUpperCase();
//
//                    // 1) Semester
//                    Semester sem = semesterService
//                            .findByYearAndTerm(year, termStr)
//                            .orElseThrow(() -> new IllegalArgumentException(
//                                    "Semester not found: "+year+" "+termStr));
//
//                    // 2) Course
//                    String fullCode = deptCode + "-" + courseNo;
//                    Course course = courseRepo
//                            .findByCourseCodeIgnoreCase(fullCode)
//                            .orElseThrow(() -> new IllegalArgumentException(
//                                    "Course not found: "+fullCode));
//
//                    // 3) Offering (find or create)
//                    CourseOffering off = offeringService
//                            .getByCourseAndSemester((long) course.getCourseId(), sem.getId())
//                            .orElseGet(() -> {
//                                CourseOffering nf = new CourseOffering();
//                                nf.setCourse(course);
//                                nf.setSemester(sem);
//                                return offeringService.create(nf);
//                            });
//
//                    // 4) Section (must already exist)
//                    String secCode = fullCode + "-" + sectionNo;
//                    Section section = repo
//                            .findBySectionCodeIgnoreCase(secCode)
//                            .orElseThrow(() -> new IllegalArgumentException(
//                                    "Section not found: "+secCode));
//
//                    // 5) Attach person as Student or TA
//                    if (studentRepo.existsById(personId)) {
//                        Student s = studentService.getStudentById(personId);
//                        section.getRegisteredStudents().add(s);
//
//                    } else if (taRepo.existsById(personId)) {
//                      /*   TA ta = taService.getTAById(personId);
//                        section.getRegisteredTas().add(ta); */
//
//                    } else {
//                        throw new IllegalArgumentException(
//                                "No student or TA with id: " + personId);
//                    }
//
//                    successful.add(section);
//
//                } catch (Exception ex) {
//                    failed.add(new FailedRowInfo(
//                            row.getRowNum(),
//                            ex.getClass().getSimpleName()+": "+ex.getMessage()
//                    ));
//                }
//            }
//        }
//
//        if (!successful.isEmpty()) {
//            repo.saveAll(successful);
//            repo.flush();
//        }
//
//        return Map.of(
//                "successCount", successful.size(),
//                "failedCount",  failed.size(),
//                "failedRows",   failed
//        );
//    }


    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue()).trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA -> cell.getCellFormula(); // Optional: handle formulas
            default -> throw new IllegalStateException("Unexpected cell type: " + cell.getCellType());
        };
    }

    private double getNumericCellValue(Cell cell) {
        if (cell == null) throw new IllegalArgumentException("Cell is null");
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> Double.parseDouble(cell.getStringCellValue().trim());
            default -> throw new IllegalStateException("Expected numeric cell, but got: " + cell.getCellType());
        };
    }

    private long getLongCellValue(Cell cell) {
        return (long) getNumericCellValue(cell);
    }


    @Transactional
    @Override
    public boolean assignTA(Long taId, String sectionCode) {
        Section section = sectionRepo
                .findBySectionCodeIgnoreCase(sectionCode)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionCode));

        // get through the service so you pick up any TA‑specific logic
        TA ta = taService.getTAByIdTa(taId);

        if (section.getAssignedTas().contains(ta)) {
            throw new IllegalStateException(
                    "TA " + taId + " is already assigned to section " + sectionCode);
        }

        section.getAssignedTas().add(ta); // hope it makes that
        sectionRepo.save(section);
        return true;
    }
}
