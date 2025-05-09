package com.example.service;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.dto.ClassRoomDto;
import com.example.entity.General.Event;

public interface ClassRoomServ {
    Map<String, Object> importClassRoomsFromExcel(MultipartFile file) throws IOException;

    ClassRoomDto create(ClassRoomDto dto);

    ClassRoomDto update(String id, ClassRoomDto dto);

    void delete(String id);

    ClassRoomDto getById(String id);

    List<ClassRoomDto> getAll();

    List<ClassRoomDto> getClassRoomsByTime(Event duration);
}