package com.example.service;

import com.example.entity.Courses.Section;
import java.util.List;

public interface SectionServ {
    Section create(Section section);
    Section update(Integer id, Section section);
    Section getById(Integer id);
    List<Section> getAll();
    void delete(Integer id);
}