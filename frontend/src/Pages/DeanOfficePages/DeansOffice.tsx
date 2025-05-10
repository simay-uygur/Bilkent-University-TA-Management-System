import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import styles from './DeansOffice.module.css'

interface DeptStats {
  dept: string
  instructors: number
  courses: number
  tas: number
}

const DEPARTMENTS: DeptStats[] = [
  { dept: 'CS', instructors: 24, courses: 42, tas: 76 },
  { dept: 'IE', instructors: 18, courses: 35, tas: 52 },
  { dept: 'EEE', instructors: 29, courses: 48, tas: 87 },
  { dept: 'ME', instructors: 22, courses: 39, tas: 65 }
]

const DeansOfficePage: React.FC = () => {
  const navigate = useNavigate()

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>Engineering Faculty Overview</h1>
      <div className={styles.grid}>
        {DEPARTMENTS.map(dept => (
          <div
            key={dept.dept}
            className={styles.card}
            onClick={() => navigate(`/deans-office/department/${dept.dept}`)}
          >
            <h2 className={styles.deptName}>{dept.dept}</h2>
            <p className={styles.metric}>Instructors: {dept.instructors}</p>
            <p className={styles.metric}>Courses: {dept.courses}</p>
            <p className={styles.metric}>TAs: {dept.tas}</p>
          </div>
        ))}
      </div>
    </div>
  )
}

export default DeansOfficePage
/* // src/pages/DeansOfficePage.tsx
import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

import styles from './DeansOffice.module.css'

interface DeptStats {
  dept: string
  instructors: number
  courses: number
  tas: number
}


const DEPARTMENTS = ['CS', 'IE', 'EEE', 'ME']

const DeansOfficePage: React.FC = () => {
  const [stats, setStats] = useState<DeptStats[]>([])
  const navigate = useNavigate()

  useEffect(() => {
    async function loadStats() {
      const all = await Promise.all(DEPARTMENTS.map(async dept => {
        const [ins, cou, tas] = await Promise.all([
          fetch(`/api/instructors/department/${dept}`).then(r => r.json()),
          fetch(`/api/v1/offerings/department/${dept}`).then(r => r.json()),
          fetch(`/api/ta/department/${dept}`).then(r => r.json()),
        ])
        return {
          dept,
          instructors: Array.isArray(ins) ? ins.length : 0,
          courses:     Array.isArray(cou) ? cou.length : 0,
          tas:         Array.isArray(tas) ? tas.length : 0,
        }
      }))
      setStats(all)
    }
    loadStats().catch(console.error)
  }, [])

  return (
    <>
      

      <div className={styles.pageWrapper}>
        <h1 className={styles.heading}>Engineering Faculty Overview</h1>
        <div className={styles.grid}>
          {stats.map(s => (
            <div
              key={s.dept}
              className={styles.card}
              onClick={() => navigate(`/deans-office/department/${s.dept}`)}
            >
              <h2 className={styles.deptName}>{s.dept}</h2>
              <p className={styles.metric}>Instructors: {s.instructors}</p>
              <p className={styles.metric}>Courses: {s.courses}</p>
              <p className={styles.metric}>TAs: {s.tas}</p>
            </div>
          ))}
        </div>
      </div>
    </>
  )
}

export default DeansOfficePage */



/* import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './DeansOffice.module.css';

interface Course {
  id: number;
  code: string;
  name: string;
  examTime: string;
  location: string;
  numProctorsRequired: number;
}

const courses: Course[] = [
  { id: 1, code: 'CS-101', name: 'Intro to Programming', examTime: '2025-06-15 10:00', location: 'Room 101', numProctorsRequired: 2 },
  { id: 2, code: 'ENG-202', name: 'Technical Writing',    examTime: '2025-06-16 14:00', location: 'Room 102', numProctorsRequired: 1 },
];

const DeansOffice: React.FC = () => {
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
                onClick={() => navigate(`assign/${c.id}/manual`)}
              >
                Manual Assign
              </button>
              <button
                className={styles.btn}
                onClick={() => navigate(`assign/${c.id}/automatic`)}
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

export default DeansOffice;
 */