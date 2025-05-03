package com.example.service;

import com.example.dto.FailedRowInfo;
import com.example.entity.Actors.Instructor;
import com.example.entity.Actors.TA;
import com.example.entity.Courses.Course;
import com.example.entity.Courses.CourseOffering;
import com.example.entity.Courses.Section;
import com.example.entity.General.Semester;
import com.example.entity.General.Student;
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

    @Override
    public Section create(Section section) {
        // Validate sectionCode uniqueness
        if (repo.existsBySectionCodeEqualsIgnoreCase(section.getSectionCode())) { //hope it works
            throw new IllegalArgumentException("Section with code '" + section.getSectionCode() + "' already exists.");
        }

        // Validate presence of required associations
        if (section.getInstructor() == null || section.getInstructor().getId() == null) {
            throw new IllegalArgumentException("Instructor must be specified.");
        }

        if (section.getOffering() == null || section.getOffering().getId() == null) {
            throw new IllegalArgumentException("Offering must be specified.");
        }

        // Optional: You may fetch the actual instructor and offering from DB to ensure they exist
        // e.g., instructorRepo.findById(id).orElseThrow(...)
        // and set them again if needed

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

    @Override
    public Section getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + id));
    }

    @Override
    public List<Section> getAll() {
        return repo.findAll();
    }

    @Override
    public void delete(Integer id) {
        repo.deleteById(id);
    }

    @Override
    //@Transactional
    public Map<String,Object> importFromExcel(MultipartFile file) throws IOException {
        List<Section> successful = new ArrayList<>();
        List<FailedRowInfo> failed  = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // header

                try {
                    String deptCode = row.getCell(0).getStringCellValue().trim();
                    int    courseNo = (int) row.getCell(1).getNumericCellValue();
                    int    sectionNo= (int) row.getCell(2).getNumericCellValue();
                    int    year     = (int) row.getCell(3).getNumericCellValue();
                    String termStr  = row.getCell(4).getStringCellValue().trim().toUpperCase();
                    long   staffId  = (long) row.getCell(5).getNumericCellValue();

                    // 1) semester
                    Semester sem = semesterService
                            .findByYearAndTerm(year, termStr)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Semester not found: " + year + " " + termStr));
                    // assume deptCode = "CS", courseNo = "319"
                    String fullCode = deptCode.toUpperCase() + "-" + courseNo;
                    // e.g. "CS‑319"
                    Course course = courseRepo
                            .findByCourseCodeIgnoreCase(fullCode)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Course not found: " + fullCode
                            ));
                    // 3) offering
                    CourseOffering off = offeringService
                            .getByCourseAndSemester((long) course.getCourseId(), sem.getId())
                            .orElseGet(() -> {
                                // build a brand‑new offering for this course + semester
                                CourseOffering newOff = new CourseOffering();
                                newOff.setCourse(course);
                                newOff.setSemester(sem);
                                // this will persist it (and because of your unique constraint
                                // on (course,semester) it won't duplicate if another thread snuck one
                                // in between)
                                return offeringService.create(newOff);
                            });
                    // 4) instructor
                    Instructor instr = instructorService.getById(staffId);

                    // 5) build section
                    Section sec = new Section();
                    sec.setSectionCode(course.getCourseCode() + "-" + sectionNo);
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

    //@Transactional
    @Override
    public Map<String,Object> importSectionStudentsFromExcel(MultipartFile file) throws IOException {
        List<Section> successful = new ArrayList<>();
        List<FailedRowInfo> failed    = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header

                try {
                    long   personId  = getLongCellValue(row.getCell(0));
                    String deptCode  = getStringCellValue(row.getCell(1)).toUpperCase();
                    int    courseNo  = (int) getNumericCellValue(row.getCell(2));
                    int    sectionNo = (int) getNumericCellValue(row.getCell(3));
                    int    year      = (int) getNumericCellValue(row.getCell(4));
                    String termStr   = getStringCellValue(row.getCell(5)).toUpperCase();
//                    long   personId  = (long) row.getCell(0).getNumericCellValue();
//                    String deptCode  = row.getCell(1).getStringCellValue().trim().toUpperCase();
//                    int    courseNo  = (int) row.getCell(2).getNumericCellValue();
//                    int    sectionNo = (int) row.getCell(3).getNumericCellValue();
//                    int    year      = (int) row.getCell(4).getNumericCellValue();
//                    String termStr   = row.getCell(5).getStringCellValue().trim().toUpperCase();

                    // 1) Semester
                    Semester sem = semesterService
                            .findByYearAndTerm(year, termStr)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Semester not found: "+year+" "+termStr));

                    // 2) Course
                    String fullCode = deptCode + "-" + courseNo;
                    Course course = courseRepo
                            .findByCourseCodeIgnoreCase(fullCode)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Course not found: "+fullCode));

                    // 3) Offering (find or create)
                    CourseOffering off = offeringService
                            .getByCourseAndSemester((long) course.getCourseId(), sem.getId())
                            .orElseGet(() -> {
                                CourseOffering nf = new CourseOffering();
                                nf.setCourse(course);
                                nf.setSemester(sem);
                                return offeringService.create(nf);
                            });

                    // 4) Section (must already exist)
                    String secCode = fullCode + "-" + sectionNo;
                    Section section = repo
                            .findBySectionCodeIgnoreCase(secCode)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Section not found: "+secCode));

                    // 5) Attach person as Student or TA
                    if (studentRepo.existsById(personId)) {
                        Student s = studentService.getStudentById(personId);
                        section.getStudents().add(s);

                    } else if (taRepo.existsById(personId)) {
                        TA ta = taService.getTAById(personId);
                        section.getTaAsStudents().add(ta);

                    } else {
                        throw new IllegalArgumentException(
                                "No student or TA with id: " + personId);
                    }

                    successful.add(section);

                } catch (Exception ex) {
                    failed.add(new FailedRowInfo(
                            row.getRowNum(),
                            ex.getClass().getSimpleName()+": "+ex.getMessage()
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
}