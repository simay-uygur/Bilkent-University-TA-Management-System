package com.example.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.dto.TaDto;
import com.example.dto.TaskDto;
import com.example.entity.Courses.Section;

import jakarta.transaction.Transactional;

public interface SectionServ {
    Section create(Section section);
    Section update(Integer id, Section section);
    Section getById(Integer id);
    List<Section> getAll();
    void delete(Integer id);
    Map<String,Object> importFromExcel(MultipartFile file) throws IOException;
    List<TaskDto> getAllTasks(int sectionNumber, String courseCode);
    //@Transactional
    //Map<String, Object> importSectionsFromExcel(MultipartFile file) throws IOException;

    //@Transactional
    Map<String,Object> importSectionStudentsFromExcel(MultipartFile file) throws IOException;
    Section getBySectionCode(String sectionCode);

    @Transactional
    Map<String,Object> importSectionsAndInstructorsExcelWithCoordinators(MultipartFile file) throws IOException;

    boolean assignTA(Long taId, String sectionCode);
    //List<SectionDto> getByDepartment(String deptName);
    List<TaskDto> getTasks(String sectionCode);
}