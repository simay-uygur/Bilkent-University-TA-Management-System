package com.example.service;

import com.example.entity.Courses.Section;
import com.example.repo.SectionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionServImpl implements SectionServ {

    private final SectionRepo repo;

    @Override
    public Section create(Section section) {
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
}