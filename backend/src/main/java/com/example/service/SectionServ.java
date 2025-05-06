package com.example.service;

import com.example.dto.SectionDto;
import com.example.entity.Courses.Section;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SectionServ {
    Section create(Section section);
    Section update(Integer id, Section section);
    Section getById(Integer id);
    List<Section> getAll();
    void delete(Integer id);
    Map<String,Object> importFromExcel(MultipartFile file) throws IOException;

    //@Transactional
    //Map<String, Object> importSectionsFromExcel(MultipartFile file) throws IOException;

    //@Transactional
    Map<String,Object> importSectionStudentsFromExcel(MultipartFile file) throws IOException;
    Section getBySectionCode(String sectionCode);

    @Transactional
    Map<String,Object> importSectionsAndInstructorsExcelWithCoordinators(MultipartFile file) throws IOException;

    @Transactional
    boolean assignTA(Long taId, String sectionCode);
    //List<SectionDto> getByDepartment(String deptName);
}