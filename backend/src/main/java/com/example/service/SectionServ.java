package com.example.service;

import com.example.entity.Courses.Section;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SectionServ {
    Section create(Section section);
    Section update(Integer id, Section section);
    Section getById(Integer id);
    List<Section> getAll();
    void delete(Integer id);
    Map<String,Object> importFromExcel(MultipartFile file) throws IOException;
}