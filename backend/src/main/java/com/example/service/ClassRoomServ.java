package com.example.service;


import com.example.dto.ClassRoomDto;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ClassRoomServ {
    Map<String, Object> importClassRoomsFromExcel(MultipartFile file) throws IOException;

    ClassRoomDto create(ClassRoomDto dto);

    ClassRoomDto update(String id, ClassRoomDto dto);

    void delete(String id);

    ClassRoomDto getById(String id);

    List<ClassRoomDto> getAll();
}