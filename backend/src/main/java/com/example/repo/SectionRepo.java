package com.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Courses.Section;

public interface SectionRepo extends JpaRepository<Section, Integer>{
}
