package com.example.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.Exams.Exam;
import com.example.entity.Exams.ExamRoom;
import com.example.entity.Exams.ExamRoom_DTO;
import com.example.entity.Exams.Exam_DTO;
import com.example.entity.General.Student;
import com.example.entity.General.Student_DTO;
import com.example.repo.ExamRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ExamServImpl implements ExamServ{

    private ExamRepo examRepo;

    @Override
    public Exam_DTO getExam(Exam exam) {
        Exam_DTO dto = new Exam_DTO();
        List<ExamRoom_DTO> rooms = new ArrayList<>();
        for (ExamRoom room : exam.getExam_rooms()){
            List<Student_DTO> studDtos = new ArrayList<>();
            for(Student stud : room.getStudents_list()){
                Student_DTO studDto = new Student_DTO(stud.getStudent_id(), stud.getStudent_name(),stud.getStudent_surname());
                studDtos.add(studDto);
            }
            ExamRoom_DTO room_DTO = new ExamRoom_DTO(room.getExam_room().getClass_code(),studDtos);
            rooms.add(room_DTO);
        }
        dto.setCourse(exam.getTask().getCourse().getCourse_code());
        dto.setDuration(exam.getTask().getDuration().toString());
        dto.setExam_rooms(rooms);
        return dto;
    }

    @Override
    public boolean createExam(Exam exam) {
        examRepo.save(exam);
        return examRepo.existsById(exam.getExam_id());
    }
    
}
