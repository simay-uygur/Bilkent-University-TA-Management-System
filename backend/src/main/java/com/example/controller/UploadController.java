package com.example.controller;

import com.example.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController {

    private final StudentServ studentServ;
    private final TAServ taServ;
    private final CourseServ courseServ;
    private final UploadService uploadService;
    private final SectionServ sectionService;
    private final InstructorServ instructorServ;
    private final ClassRoomServ classRoomService;
    private final LessonServ lessonService;
    private final ExamServ examService;

    //for uploading students and ta's (from the same excel file)
    @PostMapping("/students")
    public ResponseEntity<Map<String, Object>> uploadStudents(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = studentServ.importStudentsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    //for uploading only  TA's from excel file' - not used anymore!!! just for testing    - - -
    @PostMapping("/tas")
    public ResponseEntity<Map<String, Object>> uploadTAs(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = taServ.importTAsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PostMapping("/courses")
    public ResponseEntity<Map<String, Object>> uploadCourses(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = courseServ.importCoursesFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // for both instructors and department staff (from the same excel file)
    @PostMapping("/staff")
    public ResponseEntity<Map<String, Object>> uploadInstructorsAndStaff(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = uploadService.importInstructorsAndStaffFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PostMapping("/instructors")
    public ResponseEntity<Map<String, Object>> uploadInstructors(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = instructorServ.importInstructorsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/sections-instructor")
    public ResponseEntity<?> importCourseSectionInstructorRelations(@RequestParam("file") MultipartFile file) {
        try {
            // now we _capture_ the real result map
            Map<String,Object> result = sectionService.importFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // bad data in Excel
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "status",  "error",
                            "message", e.getMessage()
                    ));
        } catch (IOException e) {
            // file‐I/O problem
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status",  "error",
                            "message", "Could not read file: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/sections-student")
    public ResponseEntity<?> importCourseSectionStudentRelations(@RequestParam("file") MultipartFile file) {
        try {
            Map<String,Object> result = sectionService.importSectionStudentsFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "status",  "error",
                            "message", e.getMessage()
                    ));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status",  "error",
                            "message", "Could not read file: " + e.getMessage()
                    ));
        }
    }

    //to upload classrooms from excel file
    @PostMapping("/classrooms")
    public ResponseEntity<?> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(classRoomService.importClassRoomsFromExcel(file));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File processing failed: " + e.getMessage());
        }
    }


    @PostMapping("/lessons")
    public ResponseEntity<Map<String, Object>> importLessons(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = lessonService.importLessonsFromExcel(file);
        return ResponseEntity.ok(result);
    }


    @PostMapping(
            "sections-instructor-coordinator"
            //value = "/import",
            //consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            //produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String,Object>> importSectionsFromExcel(
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        Map<String,Object> report = sectionService.importSectionsAndInstructorsExcelWithCoordinators(file);
        return ResponseEntity.ok(report);
    }

    /**
     * Import exams from an Excel file.
     * @param file multipart Excel (.xlsx) upload
     * @return map with successCount, failedCount, and failedRows
     */
    @PostMapping("/exams")
    public ResponseEntity<Map<String,Object>> importExams(
            @RequestParam("file") MultipartFile file) throws IOException {

        Map<String,Object> result = examService.importExamsFromExcel(file);
        return ResponseEntity.ok(result);
    }


}