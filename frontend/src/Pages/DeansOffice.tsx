import React, { useState, useEffect } from 'react';
import NavBarDeans from '../components/NavBarDeans';
import styles from './DeansOffice.module.css';

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

// 1) Your awaiting tasks:
const tasks: Task[] = [
  { id: 1, code: 'CS-101', name: 'Intro to Programming', examTime: '2025-06-15 10:00', location: 'Room 101', numProctorsRequired: 2 },
  { id: 2, code: 'ENG-202', name: 'Technical Writing',   examTime: '2025-06-16 14:00', location: 'Room 102', numProctorsRequired: 1 },
    { id: 3, code: 'MATH-150', name: 'Calculus I',         examTime: '2025-06-17 09:00', location: 'Room 103', numProctorsRequired: 3 },
    { id: 4, code: 'PHY-110', name: 'General Physics',    examTime: '2025-06-18 11:00', location: 'Room 104', numProctorsRequired: 2 },
    { id: 5, code: 'BIO-101', name: 'Biology Basics',     examTime: '2025-06-19 13:00', location: 'Room 105', numProctorsRequired: 1 },
    { id: 6, code: 'CHE-105', name: 'Organic Chemistry',  examTime: '2025-06-20 15:00', location: 'Room 106', numProctorsRequired: 2 },
    { id: 7, code: 'CS-201', name: 'Data Structures',     examTime: '2025-06-21 10:00', location: 'Room 107', numProctorsRequired: 3 },
    { id: 8, code: 'CS-301', name: 'Algorithms',         examTime: '2025-06-22 14:00', location: 'Room 108', numProctorsRequired: 2 },
];

// 2) Mock TAs for selection:
const mockTAs: TA[] = [
  { id: 1, name: 'Alice Smith',    workload: 3 },
  { id: 2, name: 'Bob Johnson',    workload: 1 },
  { id: 3, name: 'Carol Williams', workload: 2 },
  { id: 4, name: 'David Brown',    workload: 0 },
];

export default function DeansOffice() {
  // `active` holds the task + mode when user clicks a button
  const [active, setActive] = useState<{ task: Task; mode: 'automatic' | 'manual' } | null>(null);

  // selection UI state
  const [tas, setTAs] = useState<TA[]>([]);
  const [selected, setSelected] = useState<number[]>([]);

  // when a task+mode is activated, load TAs and pre-select if automatic
  useEffect(() => {
    if (!active) return;
    setTAs(mockTAs);

    if (active.mode === 'automatic') {
      // pick the N with least workload
      const auto = [...mockTAs]
        .sort((a, b) => a.workload - b.workload)
        .slice(0, active.task.numProctorsRequired)
        .map(t => t.id);
      setSelected(auto);
    } else {
      setSelected([]);
    }
  }, [active]);

  // toggles for manual mode
  const toggleTA = (id: number) => {
    if (!active) return;
    const req = active.task.numProctorsRequired;
    setSelected(sel =>
      sel.includes(id)
        ? sel.filter(x => x !== id)
        : sel.length < req
        ? [...sel, id]
        : sel
    );
  };

  // confirm assignment
  const confirm = () => {
    if (!active) return;
    const chosen = tas.filter(t => selected.includes(t.id)).map(t => t.name).join(', ');
    alert(
      `${active.mode === 'automatic' ? 'Auto' : 'Manual'} assigned for ${active.task.code}: ${chosen}`
    );
    setActive(null);
  };

  // --------------- RENDER ---------------

  // 1) Awaiting list
  if (!active) {
    return (
      <div className={styles.pageWrapper}>
    

        <h1 className={styles.heading}>Awaiting Proctor Assignments</h1>
        <div className={styles.taskList}>
          {tasks.map(task => (
            <div key={task.id} className={styles.taskCard}>
              <div className={styles.info}>
                <strong>{task.code}</strong> — {task.name}
                <div>Exam: {task.examTime}</div>
                <div>Location: {task.location}</div>
                <div>Needed: {task.numProctorsRequired}</div>
              </div>
              <div className={styles.actions}>
                <button
                  className={styles.btnAuto}
                  onClick={() => setActive({ task, mode: 'automatic' })}
                >
                  Auto Assign
                </button>
                <button
                  className={styles.btnManual}
                  onClick={() => setActive({ task, mode: 'manual' })}
                >
                  Manual Assign
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  // 2) Selection UI for the active.task
  const { task, mode } = active;
  const req = task.numProctorsRequired;

  return (
    <div className={styles.pageWrapper}>
    

      <button className={styles.back} onClick={() => setActive(null)}>
        ← Back
      </button>
      <h1 className={styles.heading}>
        {mode === 'automatic' ? 'Automatic' : 'Manual'} Assign for {task.code}
      </h1>
      <p className={styles.note}>
        Select exactly {req} proctor{req > 1 ? 's' : ''}.
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
                    onChange={() => toggleTA(t.id)}
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
        disabled={selected.length !== req}
        onClick={confirm}
      >
        Confirm Assignment
      </button>
    </div>
  );
}
