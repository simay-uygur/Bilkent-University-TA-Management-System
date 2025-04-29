// src/components/ManageWorkload.tsx
import React, { useState, ChangeEvent, FormEvent } from 'react';
import InsNavBar from '../components/InsNavBar';
import styles from './ManageWorkload.module.css';

interface Task {
  id: number;
  title: string;
  date: string;
  time: string;
  type: 'Citation' | 'Proctoring' | 'Lab';
  status: 'pending' | 'approved';
}

// 3 mock tasks
const initialTasks: Task[] = [
  { id: 1, title: 'Mock Lab Setup',       date: '2025-06-01', time: '10:00', type: 'Lab',        status: 'pending'  },
  { id: 2, title: 'Mock Citation Review', date: '2025-06-02', time: '14:00', type: 'Citation',   status: 'approved' },
  { id: 3, title: 'Mock Proctoring',      date: '2025-06-03', time: '12:00', type: 'Proctoring', status: 'pending'  },
];

export default function ManageWorkload() {
  const [tasks, setTasks] = useState<Task[]>(initialTasks);
  const [modalOpen, setModalOpen] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [current, setCurrent] = useState<Partial<Task>>({
    title: '',
    date: '',
    time: '',
    type: 'Citation',
    status: 'pending',
  });

  // open modal for adding
  const openAdd = () => {
    setCurrent({ title:'', date:'', time:'', type:'Citation', status:'pending' });
    setIsEdit(false);
    setModalOpen(true);
  };

  // open modal for editing
  const openEdit = (t: Task) => {
    setCurrent({ ...t });
    setIsEdit(true);
    setModalOpen(true);
  };

  // handle form field changes
  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setCurrent(c => ({ ...c, [name]: value }));
  };

  // save new or edited task
  const handleSave = (e: FormEvent) => {
    e.preventDefault();
    if (!current.title || !current.date || !current.time || !current.type) return;
    if (isEdit && current.id != null) {
      setTasks(ts =>
        ts.map(t =>
          t.id === current.id
            ? {
                ...t,
                title: current.title as string,
                date: current.date as string,
                time: current.time as string,
                type: current.type as Task['type'],
                status: current.status as Task['status'],
              }
            : t
        )
      );
    } else {
      const nextId = Math.max(...tasks.map(t => t.id), 0) + 1;
      setTasks(ts => [
        ...ts,
        {
          id: nextId,
          title: current.title as string,
          date: current.date as string,
          time: current.time as string,
          type: current.type as Task['type'],
          status: current.status as Task['status'],
        },
      ]);
    }
    setModalOpen(false);
  };

  // delete a task
  const handleDelete = (id: number) => {
    setTasks(ts => ts.filter(t => t.id !== id));
  };

  return (
    <div className={styles.pageWrapper}>
      <InsNavBar />

      <h1 className={styles.heading}>Manage Workload</h1>
      <button className={styles.addBtn} onClick={openAdd}>
        + Add Task
      </button>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Title</th><th>Date</th><th>Time</th><th>Type</th><th>Status</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map(t => (
            <tr key={t.id}>
              <td>{t.title}</td>
              <td>{t.date}</td>
              <td>{t.time}</td>
              <td>{t.type}</td>
              <td>{t.status}</td>
              <td>
                <button className={styles.updateBtn} onClick={() => openEdit(t)}>
                  Update
                </button>
                <button className={styles.deleteBtn} onClick={() => handleDelete(t.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {modalOpen && (
        <div className={styles.modalBackdrop} onClick={() => setModalOpen(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <h2>{isEdit ? 'Edit Task' : 'New Task'}</h2>
            <form onSubmit={handleSave}>
              <label>Title</label>
              <input
                name="title"
                value={current.title || ''}
                onChange={handleChange}
                required
              />

              <label>Date</label>
              <input
                name="date"
                type="date"
                value={current.date || ''}
                onChange={handleChange}
                required
              />

              <label>Time</label>
              <input
                name="time"
                type="time"
                value={current.time || ''}
                onChange={handleChange}
                required
              />

              <label>Type</label>
              <select name="type" value={current.type} onChange={handleChange}>
                <option>Lab</option>
                <option>Citation</option>
                <option>Proctoring</option>
              </select>

              <label>Status</label>
              <select name="status" value={current.status} onChange={handleChange}>
                <option value="pending">pending</option>
                <option value="approved">approved</option>
              </select>

              <div className={styles.modalBtns}>
                <button type="button" onClick={() => setModalOpen(false)}>
                  Cancel
                </button>
                <button type="submit">
                  Save
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
