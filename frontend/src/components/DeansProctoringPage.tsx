// src/pages/ProctoringAssignmentsPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './DeansProctoringPage.module.css';

interface Course {
  id: number;
  code: string;
  name: string;
  examTime: string;
  location: string;
  numProctorsRequired: number;
}

const courses: Course[] = [
  { id: 1, code: 'CS-101', name: 'Intro to Programming',    examTime: '2025-06-15 10:00', location: 'Room 101', numProctorsRequired: 2 },
  { id: 2, code: 'ENG-202', name: 'Technical Writing',       examTime: '2025-06-16 14:00', location: 'Room 102', numProctorsRequired: 1 },
];

const DeansProctoringPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>Awaiting Proctor Assignments</h1>
      <div className={styles.courseList}>
        {courses.map(c => (
          <div key={c.id} className={styles.courseCard}>
            <div className={styles.courseInfo}>
              <strong>{c.code}</strong> — {c.name}
              <div>Exam: {c.examTime}</div>
              <div>Location: {c.location}</div>
              <div>Needed: {c.numProctorsRequired}</div>
            </div>
            <div className={styles.actions}>
              <button
                className={styles.btn}
                onClick={() => navigate(`proctor/${c.id}/manual`)}
              >
                Manual Assign
              </button>
              <button
                className={styles.btn}
                onClick={() => navigate(`proctor/${c.id}/automatic`)}
              >
                Automatic Assign
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DeansProctoringPage;
