import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavBarDeans from './NavBarDeans';
import styles from './DeanAssignProctors.module.css';

interface TA {
  id: number;
  name: string;
  workload: number;
}

const mockTAs: TA[] = [
  { id: 1, name: 'Alice Smith',    workload: 3 },
  { id: 2, name: 'Bob Johnson',    workload: 1 },
  { id: 3, name: 'Carol Williams', workload: 2 },
  { id: 4, name: 'David Brown',    workload: 0 },
];

export default function DeanAssignProctors() {
  const navigate = useNavigate();
  const { courseId, mode } = useParams<{ courseId: string; mode: 'manual'|'automatic' }>();

  const [tas, setTAs] = useState<TA[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const required = 2; // ideally fetched per course

  useEffect(() => {
    setTAs(mockTAs);
  }, []);

  useEffect(() => {
    if (mode === 'automatic') {
      const ids = [...mockTAs]
        .sort((a,b) => a.workload - b.workload)
        .slice(0, required)
        .map(t => t.id);
      setSelected(ids);
    } else {
      setSelected([]);
    }
  }, [mode]);

  const toggle = (id: number) => {
    setSelected(sel =>
      sel.includes(id)
        ? sel.filter(x=>x!==id)
        : sel.length < required
          ? [...sel, id]
          : sel
    );
  };

  const confirm = () => {
    const chosen = tas.filter(t => selected.includes(t.id)).map(t => t.name).join(', ');
    alert(`${mode==='automatic'?'Auto':'Manual'} assigned for course ${courseId}: ${chosen}`);
    navigate('/deans-office');
  };

  return (
    <div className={styles.pageWrapper}>
      <NavBarDeans onNotifications={() => {}} />

      <button className={styles.back} onClick={() => navigate(-1)}>← Back</button>
      <h1 className={styles.heading}>
        {mode === 'automatic' ? 'Automatic' : 'Manual'} Assign Proctors
      </h1>
      <p className={styles.note}>Select exactly {required} proctor{required>1?'s':''}.</p>

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
              {mode==='manual' && (
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
