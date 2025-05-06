package com.example.service;

import com.example.dto.LessonDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface LessonServ {
    Map<String, Object> importLessonsFromExcel(MultipartFile file) throws IOException;

    List<LessonDto> getAllLessons();

    LessonDto getLessonById(Long id);

    LessonDto updateLesson(Long id, LessonDto dto);

    List<LessonDto> createLessonDtoList(LessonDto dto);

    void deleteLesson(Long id);
}