package com.example.repo;

import com.example.entity.Courses.Lesson;
import com.example.entity.General.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.entity.General.ClassRoom;


@Repository
public interface LessonRepo extends JpaRepository<Lesson, Long> {
    //List<Lesson> findLessonsBySection_SectionCodeAndDayEqualsIgnoreCase(String sectionSectionCode, DayOfWeek day);
    List<Lesson> findBySection_SectionCodeIgnoreCaseAndDay(
            String sectionCode,
            DayOfWeek day
    );
    List<Lesson> findByLessonRoom(ClassRoom lessonRoom);

    // only case-insensitive on the String property; day is an enum so no IgnoreCase on it
    List<Lesson> findByLessonRoom_ClassroomIdEqualsIgnoreCaseAndDay(String classroomId, DayOfWeek day);
}