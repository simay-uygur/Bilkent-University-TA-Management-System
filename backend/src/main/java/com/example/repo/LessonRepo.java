package com.example.repo;

import com.example.entity.Courses.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.entity.General.ClassRoom;


@Repository
public interface LessonRepo extends JpaRepository<Lesson, Long> {


    List<Lesson> findByLessonRoom(ClassRoom lessonRoom);
}