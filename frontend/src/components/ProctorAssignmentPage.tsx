// src/pages/ProctorAssignmentPage.tsx
import React, { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import styles from './ProctorAssignmentPage.module.css'

interface Task {
  id: number
  code: string
  name: string
  examTime: string
  location: string
  numProctorsRequired: number
}

// static until task‐API is ready
const tasks: Task[] = [
  { id: 1, code: 'CS-101', name: 'Intro to Programming', examTime: '2025-06-15 10:00', location: 'Room 101', numProctorsRequired: 2 },
  { id: 2, code: 'ENG-202',   name: 'Technical Writing',   examTime: '2025-06-16 14:00', location: 'Room 102', numProctorsRequired: 1 },
]

interface TA {
  id: number
  name: string
}

export default function ProctorAssignmentPage() {
  const navigate = useNavigate()
  const { courseId, mode } = useParams<{ courseId?: string; mode?: 'manual' | 'automatic' }>()

  const [tas, setTAs] = useState<TA[]>([])
  const [selected, setSelected] = useState<number[]>([])

  // fetch all TAs in CS department
  useEffect(() => {
    fetch('/api/ta/department/CS')
      .then(res => res.json())
      .then((data: TA[]) => setTAs(data))
      .catch(console.error)
  }, [])

  // pre‐select first N for automatic
  useEffect(() => {
    if (mode === 'automatic' && courseId) {
      const required = tasks.find(t => t.id === Number(courseId))?.numProctorsRequired ?? 1
      setSelected(tas.slice(0, required).map(t => t.id))
    }
  }, [mode, courseId, tas])

  // toggle selection for manual mode
  const toggle = (id: number) => {
    const required = tasks.find(t => t.id === Number(courseId))?.numProctorsRequired ?? 1
    setSelected(current =>
      current.includes(id)
        ? current.filter(x => x !== id)
        : current.length < required
          ? [...current, id]
          : current
    )
  }

  const confirm = () => {
    const chosen = tas.filter(t => selected.includes(t.id)).map(t => t.name).join(', ')
    alert(`${mode} assignment for task ${courseId}:\n${chosen}`)
    navigate('/dept-office/proctor')
  }

  // if no task/mode selected → list awaiting tasks
  if (!courseId || !mode) {
    return (
      <div className={styles.pageWrapper}>
        
        <h1 className={styles.heading}>Awaiting Proctor Assignments</h1>
        <div className={styles.taskList}>
          {tasks.map(t => (
            <div key={t.id} className={styles.taskCard}>
              <div className={styles.info}>
                <strong>{t.code}</strong> — {t.name}
                <div>Exam: {t.examTime}</div>
                <div>Location: {t.location}</div>
                <div>Proctors needed: {t.numProctorsRequired}</div>
              </div>
              <div className={styles.actions}>
                <button className={styles.btn} onClick={() => navigate(`/dept-office/proctor/${t.id}/automatic`)}>
                  Auto Assign
                </button>
                <button className={styles.btn} onClick={() => navigate(`/dept-office/proctor/${t.id}/manual`)}>
                  Manual Assign
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  // otherwise → selection UI
  const required = tasks.find(t => t.id === Number(courseId))!.numProctorsRequired

  return (
    <div className={styles.pageWrapper}>

      <button className={styles.back} onClick={() => navigate(-1)}>
        ← Back
      </button>

      <h1 className={styles.heading}>
        {mode === 'automatic' ? 'Automatic' : 'Manual'} Assign Proctors
      </h1>
      <p className={styles.note}>
        Choose exactly {required} proctor{required > 1 ? 's' : ''}.
      </p>

      <table className={styles.table}>
        <thead>
          <tr>
            {mode === 'manual' && <th>Select</th>}
            <th>Name</th>
          </tr>
        </thead>
        <tbody>
          {tas.map(t => (
            <tr key={t.id}>
              {mode === 'manual' && (
                <td>
                  <input
                    type="checkbox"
                    checked={selected.includes(t.id)}
                    onChange={() => toggle(t.id)}
                  />
                </td>
              )}
              <td>{t.name}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <button
        className={styles.confirm}
        disabled={selected.length !== required}
        onClick={confirm}
      >
        Confirm Assignment
      </button>
    </div>
  )
}

/* import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import DepOfNavBar from './DepartmentOffice/NavBarDepartment';
import styles from './ProctorAssignmentPage.module.css';

interface Task {
  id: number;
  code: string;
  name: string;
  examTime: string;
  location: string;
  numProctorsRequired: number;
}

interface TA {
  id: number;
  name: string;
  workload: number;
}

// mock data
const tasks: Task[] = [
  { id: 1, code: 'CS-101', name: 'Intro to Programming', examTime: '2025-06-15 10:00', location: 'Room 101', numProctorsRequired: 2 },
  { id: 2, code: 'ENG-202', name: 'Technical Writing',   examTime: '2025-06-16 14:00', location: 'Room 102', numProctorsRequired: 1 },
];

const mockTAs: TA[] = [
  { id: 1, name: 'Alice Smith',    workload: 3 },
  { id: 2, name: 'Bob Johnson',    workload: 1 },
  { id: 3, name: 'Carol Williams', workload: 2 },
  { id: 4, name: 'David Brown',    workload: 0 },
];

export default function ProctorAssignmentsPage() {
  const navigate = useNavigate();
  const { courseId, mode } = useParams<{ courseId?: string; mode?: 'manual'|'automatic' }>();

  // **Selection UI state**
  const [tas, setTAs] = useState<TA[]>([]);
  const [selected, setSelected] = useState<number[]>([]);

  // When in selection mode, load TAs
  useEffect(() => {
    if (courseId && mode) {
      setTAs(mockTAs);
    }
  }, [courseId, mode]);

  // Pre-select for automatic
  useEffect(() => {
    if (mode === 'automatic') {
      const required = tasks.find(t => t.id === Number(courseId))?.numProctorsRequired ?? 1;
      const auto = [...mockTAs]
        .sort((a,b) => a.workload - b.workload)
        .slice(0, required)
        .map(t => t.id);
      setSelected(auto);
    }
  }, [mode, courseId]);

  // toggle for manual
  const toggle = (id: number) => {
    const required = tasks.find(t => t.id === Number(courseId))?.numProctorsRequired ?? 1;
    setSelected(sel =>
      sel.includes(id)
        ? sel.filter(x => x !== id)
        : sel.length < required
          ? [...sel, id]
          : sel
    );
  };

  // confirm assignment
  const confirm = () => {
    const chosen = tas.filter(t => selected.includes(t.id));
    alert(`${mode} assignment for course ${courseId}:\n` + chosen.map(t => t.name).join('\n'));
    navigate('/dept-office/proctor');
  };

  // **1) If no courseId/mode → show awaiting list**
  if (!courseId || !mode) {
    return (
      <div className={styles.pageWrapper}>
       

        <h1 className={styles.heading}>Awaiting Proctor Assignments</h1>
        <div className={styles.taskList}>
          {tasks.map(t => (
            <div key={t.id} className={styles.taskCard}>
              <div className={styles.info}>
                <strong>{t.code}</strong> — {t.name}
                <div>Exam: {t.examTime}</div>
                <div>Location: {t.location}</div>
                <div>Proctors needed: {t.numProctorsRequired}</div>
              </div>
              <div className={styles.actions}>
                <button className={styles.btn} onClick={() => navigate(`/dept-office/proctor/${t.id}/automatic`)}>
                  Auto Assign
                </button>
                <button className={styles.btn} onClick={() => navigate(`/dept-office/proctor/${t.id}/manual`)}>
                  Manual Assign
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  // **2) Otherwise → show selection UI**
  const required = tasks.find(t => t.id === Number(courseId))!.numProctorsRequired;

  return (
    <div className={styles.pageWrapper}>
     

      <button className={styles.back} onClick={() => navigate(-1)}>
        ← Back
      </button>

      <h1 className={styles.heading}>
        {mode === 'automatic' ? 'Automatic' : 'Manual'} Assign Proctors
      </h1>
      <p className={styles.note}>
        Choose exactly {required} proctor{required>1?'s':''}.
      </p>

      <table className={styles.table}>
        <thead>
          <tr>
            {mode === 'manual' && <th>Select</th>}
            <th>Name</th>
            <th>Workload</th>
          </tr>
        </thead>
        <tbody>
          {tas.map(t => (
            <tr key={t.id}>
              {mode === 'manual' && (
                <td>
                  <input
                    type="checkbox"
                    checked={selected.includes(t.id)}
                    onChange={() => toggle(t.id)}
                  />
                </td>
              )}
              <td>{t.name}</td>
              <td>{t.workload}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <button
        className={styles.confirm}
        disabled={selected.length !== required}
        onClick={confirm}
      >
        Confirm Assignment
      </button>
    </div>
  );
}
 */