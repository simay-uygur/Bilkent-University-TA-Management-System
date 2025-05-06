import React from 'react';
import TANavBar from '../../components/NavBars/TANavBar';
import styles from './TAViewPPage.module.css';
import BackBut from '../../components/Buttons/BackBut';


interface ProctoringTask {
  startTime: string;
  finishTime: string;
  course: string;
  courseId: string;
  building: string;
  year: number;
  month: number;
  day: number;
}

// Sample data
const proctoringTasks: ProctoringTask[] = [
  { startTime: '08:00', finishTime: '10:30', course: 'Physics',    courseId: '101', building: 'A-127', year: 2025, month: 4, day: 5 },
  { startTime: '10:30', finishTime: '12:00', course: 'CS102',      courseId: '202', building: 'B-202', year: 2025, month: 4, day: 1 },
  { startTime: '08:00', finishTime: '10:30', course: 'Biology',    courseId: '303', building: 'C-150', year: 2025, month: 4, day: 1 },
  { startTime: '10:30', finishTime: '12:00', course: 'Math101',    courseId: '404', building: 'D-250', year: 2025, month: 4, day: 9 },
];

const formatDate = (y: number, m: number, d: number) =>
  `${d.toString().padStart(2,'0')}/${m.toString().padStart(2,'0')}/${y}`;

const TAViewPPage: React.FC = () => {
  const sorted = [...proctoringTasks].sort((a, b) => {
    const da = new Date(a.year, a.month - 1, a.day).getTime();
    const db = new Date(b.year, b.month - 1, b.day).getTime();
    return da - db;
  });

  return (
    <div className={styles.pageWrapper}>
      <TANavBar />
      <BackBut to="/ta" />
      <div className={styles.mainContainer}>
        <h2>Proctoring Schedule</h2>
        <table className={styles.scheduleTable}>
          <thead>
            <tr>
              <th>Date</th>
              <th>Course</th>
              <th>Course ID</th>
              <th>Time</th>
              <th>Building</th>
            </tr>
          </thead>
          <tbody>
            {sorted.map((t, idx) => (
              <tr key={idx}>
                <td>{formatDate(t.year, t.month, t.day)}</td>
                <td>{t.course}</td>
                <td>{t.courseId}</td>
                <td>{`${t.startTime} – ${t.finishTime}`}</td>
                <td>{t.building}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TAViewPPage;
