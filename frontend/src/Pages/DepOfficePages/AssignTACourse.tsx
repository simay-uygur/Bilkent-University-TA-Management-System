// src/pages/AssignTACourse/AssignTACourse.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DepOfNavBar from '../../components/NavBars/DepOfNavBar';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignTACourse.module.css';

interface Course {
  id: string;
  name: string;
  neededTAs: number;
  preferredCount: number;
  tasLeft: number;
}

const AssignTACourse: React.FC = () => {
  const navigate = useNavigate();

  const initialCourses: Course[] = [
    { id: 'CS-101', name: 'Intro to CS', neededTAs: 3, preferredCount: 2, tasLeft: 1 },
    { id: 'MATH-201', name: 'Calculus II', neededTAs: 2, preferredCount: 1, tasLeft: 2 },
    { id: 'PHY-301', name: 'Physics III', neededTAs: 1, preferredCount: 0, tasLeft: 0 },
  ];

  const [courses, setCourses] = useState<Course[]>(initialCourses);
  const [confirmId, setConfirmId] = useState<string | null>(null);
  const [confirmMsg, setConfirmMsg] = useState<string | null>(null);

  const handleAssign = (id: string) => navigate(`/asgnTA/${id}`);

  const handleFinish = (id: string) => {
    const c = courses.find(c => c.id === id);
    if (!c) return;
    setConfirmMsg(
      c.tasLeft > 0
        ? `Still ${c.tasLeft} TA${c.tasLeft > 1 ? 's' : ''} left. Are you sure to finish anyway?`
        : 'Mark this course assignment finished?'
    );
    setConfirmId(id);
  };

  const handleConfirm = () => {
    if (confirmId) setCourses(prev => prev.filter(c => c.id !== confirmId));
    setConfirmId(null);
    setConfirmMsg(null);
  };

  return (
    <div className={styles.pageWrapper}>
      <DepOfNavBar />

      <div className={styles.headerRow}>
        <BackBut to="/man" />
        <h1 className={styles.title}>Assign TAs to Course</h1>
      </div>

      <div className={styles.container}>
        <table className={styles.table}>
          <thead className={styles.headings}>
            <tr>
              <th>Course Name</th>
              <th>Course ID</th>
              <th>Needed TAs</th>
              <th>Preferred TAs</th>
              <th>TAs Left</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {courses.map(course => (
              <tr
                key={course.id}
                className={`${styles.rowBase} ${
                  course.tasLeft === 0
                    ? styles.completedRow
                    : styles.incompleteRow
                }`}
              >
                <td>{course.name}</td>
                <td>{course.id}</td>
                <td>{course.neededTAs}</td>
                <td>{course.preferredCount}</td>
                <td>{course.tasLeft}</td>
                <td className={styles.actionsCell}>
                  <button
                    className={styles.assignBtn}
                    onClick={() => handleAssign(course.id)}
                  >
                    Assign TA
                  </button>
                  <button
                    className={styles.finishBtn}
                    onClick={() => handleFinish(course.id)}
                  >
                    Finish Assignment
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {confirmMsg && (
        <ConPop
          message={confirmMsg}
          onConfirm={handleConfirm}
          onCancel={() => setConfirmMsg(null)}
        />
      )}
    </div>
  );
};

export default AssignTACourse;
