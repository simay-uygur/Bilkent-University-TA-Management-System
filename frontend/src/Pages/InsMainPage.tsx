// src/components/InsMainPage.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
//import InsNavBar from '../components/InsNavBar';
import CourseInfoPanel from '../components/CourseInfoPanel';
import styles from './InsMainPage.module.css';

export interface Instructor {
  name: string;
  surname: string;
}

export interface Course {
  id: number;
  code: string;
  name: string;
  sec: number;
  studentNum: number;
  instructor: Instructor;
}

const courses: Course[] = [
  { id: 1, code: 'CS-101', name: 'Intro to Programming', sec: 3, studentNum: 30, instructor: { name: 'John', surname: 'Doe' } },
  { id: 2, code: 'ENG-202', name: 'Technical Writing',  sec: 3, studentNum: 25, instructor: { name: 'Jane', surname: 'Smith' } },
  { id: 3, code: 'MATH-150', name: 'Calculus I',        sec: 3, studentNum: 28, instructor: { name: 'Alan', surname: 'Turing' } },
  { id: 4, code: 'PHY-110', name: 'General Physics',    sec: 3, studentNum: 32, instructor: { name: 'Marie', surname: 'Curie' } },
    { id: 5, code: 'BIO-101', name: 'Biology Basics',     sec: 3, studentNum: 20, instructor: { name: 'Charles', surname: 'Darwin' } },
    { id: 6, code: 'CHE-105', name: 'Organic Chemistry',   sec: 3, studentNum: 22, instructor: { name: 'Dmitri', surname: 'Mendeleev' } },
    { id: 7, code: 'CS-201', name: 'Data Structures',     sec: 3, studentNum: 35, instructor: { name: 'Ada', surname: 'Lovelace' } },
    { id: 8, code: 'CS-301', name: 'Algorithms',          sec: 3, studentNum: 27, instructor: { name: 'Donald', surname: 'Knuth' } },
    { id: 9, code: 'CS-401', name: 'Operating Systems',    sec: 3, studentNum: 29, instructor: { name: 'Linus', surname: 'Torvalds' } },
    { id: 10, code: 'CS-501', name: 'Machine Learning',    sec: 3, studentNum: 26, instructor: { name: 'Geoffrey', surname: 'Hinton' } },
];

const InsMainPage: React.FC = () => {
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const navigate = useNavigate();

  return (
    <div className={styles.pageWrapper}>
      

      <div className={selectedCourse ? styles.blurred : ''}>
        <main className={styles.content}>
          <h1 className={styles.heading}>My Courses</h1>
          <div className={styles.courseList}>
            {courses.map(course => (
              <div key={course.id} className={styles.courseCard}>
                <div className={styles.courseInfo}>
                  <span className={styles.courseCode}>{course.code}/{course.sec}</span>
                  <span className={styles.courseName}>{course.name}</span>
                </div>
                <div className={styles.actions}>
                  <button className={styles.actionButton}>Exam Proctoring</button>
                  <button className={styles.actionButton}>Exam Printing</button>
                  <button
  className={styles.actionButton}
  onClick={() => navigate(`/instructor/workload`)}
>
  Manage Course Works
</button>

                  <button
                    className={styles.actionButton}
                    onClick={() => navigate(`/courses/${course.id}/tas`)}
                  >
                    Course TA
                  </button>
                  <button
                    className={`${styles.actionButton} ${styles.info}`}
                    onClick={() => setSelectedCourse(course)}
                  >
                    Course Info
                  </button>
                </div>
              </div>
            ))}
          </div>
        </main>
      </div>

      {selectedCourse && (
        <CourseInfoPanel
          course={selectedCourse}
          onClose={() => setSelectedCourse(null)}
        />
      )}
    </div>
  );
};

export default InsMainPage;
