import React, { useEffect, useState } from 'react';
import {
  fetchCourses,
  assignTA,
  Course
} from '../api';
import styles from './InstructorDashboard.module.css';

const InstructorDashboard: React.FC = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [selected, setSelected] = useState<Record<string,string>>({});

  useEffect(() => {
    fetchCourses().then(resp => setCourses(resp.data));
  }, []);

  const onAssign = (courseId: string) => {
    assignTA(Number(courseId), selected[courseId]).then(() => {
      // maybe refresh or show toast
    });
  };

  return (
    <>
      <header className={styles.header}>TA Management – Course Coordinator</header>
      <main className={styles.container}>
        <h2>Courses & Assignments</h2>
        {courses.map(c => (
          <div key={c.id} className={styles.card}>
            <div className={styles.cardHeader}>
              {c.code} – {c.title}
            </div>
            <p><strong>Instructor:</strong> {c.instructor}</p>
            <p><strong>Required TAs:</strong> {c.minTAs} – {c.maxTAs}</p>
            <p>
              <strong>Preferences:</strong>{' '}
              {c.mustPreferred.map(n => <span key={n} className={styles.tagMust}>Must: {n}</span>)}{' '}
              {c.avoidPreferred.map(n => <span key={n} className={styles.tagAvoid}>Avoid: {n}</span>)}
            </p>
            <div className={styles.assignRow}>
              <select
                value={selected[c.id] || ''}
                onChange={e => setSelected(prev => ({...prev, [c.id]: e.target.value}))}
              >
                <option value="">Assign TA…</option>
                {c.availableTAs.map(ta => (
                  <option key={ta.id} value={ta.id}>{ta.name}</option>
                ))}
              </select>
              <button onClick={() => onAssign(c.id)}>Assign</button>
            </div>
          </div>
        ))}
      </main>
    </>
  );
};

export default InstructorDashboard;
