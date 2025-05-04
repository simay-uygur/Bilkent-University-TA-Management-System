package com.example.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.dto.ExamDto;
import com.example.dto.ExamRoomDto;
import com.example.dto.StudentDto;
import com.example.entity.Exams.Exam;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.General.Student;
import com.example.repo.ExamRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ExamServImpl implements ExamServ{

    private final ExamRepo examRepo;

    @Override
    public ExamDto getExam(Exam exam) {
        ExamDto dto = new ExamDto();
        List<ExamRoomDto> rooms = new ArrayList<>();
        for (ExamRoom room : exam.getExam_rooms()){
            List<StudentDto> studDtos = new ArrayList<>();
            for (Student stud : room.getStudentsList()) { // some other fields may be added here if needed.
                StudentDto s = new StudentDto();
                s.setStudentId(stud.getStudentId());
                s.setStudentName(stud.getStudentName());
                s.setStudentSurname(stud.getStudentSurname());
                s.setAcademicStatus(stud.getAcademicStatus());
                s.setDepartment(stud.getDepartment());
                studDtos.add(s);
            }
            ExamRoomDto room_DTO = new ExamRoomDto(room.getExamRoom().getClassroomId(), studDtos); // hope it works
            rooms.add(room_DTO);
        }
        dto.setCourseCode(exam.getTask().getSection().getOffering().getCourse().getCourseCode()); //this can be changed
        dto.setDuration(exam.getTask().getDuration().toString());
        dto.setExamRooms(rooms);
        return dto;
    }

    @Override
    public boolean createExam(Exam exam) {
        examRepo.save(exam);
        return examRepo.existsById(exam.getExamId());
    }
    

}
