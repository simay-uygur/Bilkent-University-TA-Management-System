import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import InsNavBar from '../../components/NavBars/InsNavBar';
import CourseInfoPanel from './CourseInfoPanel';
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

const initialCourses: Course[] = [
  {
    id: 1,
    code: 'CS-101',
    name: 'Introduction to Programming',
    sec: 3,
    studentNum: 30,
    instructor: { name: 'John', surname: 'Doe' },
  },
  {
    id: 2,
    code: 'ENG-202',
    name: 'Technical Writing',
    sec: 3,
    studentNum: 30,
    instructor: { name: 'Jane', surname: 'Smith' },
  },
  {
    id: 3,
    code: 'MATH-150',
    name: 'Calculus I',
    sec: 3,
    studentNum: 30,
    instructor: { name: 'Alan', surname: 'Turing' },
  },
  {
    id: 4,
    code: 'PHY-110',
    name: 'General Physics',
    sec: 3,
    studentNum: 30,
    instructor: { name: 'Marie', surname: 'Curie' },
  },
];

const InsMainPage: React.FC = () => {
  const [courses] = useState<Course[]>(initialCourses);
  const [infoCourse, setInfoCourse] = useState<Course | null>(null);
  const navigate = useNavigate();

  return (
    <div className={styles.pageWrapper}>
      

      <main className={styles.content}>
        <h1 className={styles.heading}>My Courses</h1>
        <div className={styles.courseList}>
          {courses.map(course => (
            <div key={course.id} className={styles.courseCard}>
              <div className={styles.courseInfo}>
                <span className={styles.courseCode}>
                  {course.code}/{course.sec}
                </span>
                <span className={styles.courseName}>{course.name}</span>
              </div>
              <div className={styles.actions}>
                <button
                  className={styles.actionButton}
                  onClick={() => navigate(`/examProc/${course.code}`)}
                >
                  Exam Proctoring
                </button>
                <button className={styles.actionButton}>
                  Exam Printing
                </button>
                <button className={styles.actionButton} onClick={() => navigate(`/man/${course.code}`)} >
                  Manage Course Works
                </button>
                <button className={styles.actionButton} onClick={() => navigate(`/courseTA/${course.code}`)}>
                  Course TA
                </button>
                <button
                  className={`${styles.actionButton} ${styles.info}`}
                  onClick={() => setInfoCourse(course)}
                >
                  Course Info
                </button>
              </div>
            </div>
          ))}
        </div>
      </main>

      {infoCourse && (
        <CourseInfoPanel
          course={infoCourse}
          onClose={() => setInfoCourse(null)}
        />
      )}
    </div>
  );
};

export default InsMainPage;
