// src/components/ManageWorkload.tsx
import React, { useState, useEffect, ChangeEvent, FormEvent } from 'react';
import InsNavBar from './InsNavBar';
import { fetchAllTAs } from '../api';
import styles from './ManageWorkload.module.css';

interface Task {
  id: number;
  title: string;
  date: string;
  time: string;
  type: 'Recitation' | 'Proctoring' | 'Lab';
  status: 'pending' | 'approved';
  assignedId?: string;
}

interface TA {
  id: string;
  displayName: string;
}

const initialTasks: Task[] = [
  { id: 1, title: 'Mock Lab Setup',       date: '2025-06-01', time: '10:00', type: 'Lab',        status: 'pending',  assignedId: '' },
  { id: 2, title: 'Mock Citation Review', date: '2025-06-02', time: '14:00', type: 'Recitation',   status: 'approved', assignedId: '' },
  { id: 3, title: 'Mock Proctoring',      date: '2025-06-03', time: '12:00', type: 'Proctoring', status: 'pending',  assignedId: '' },
];

const TASK_TYPES = ['Recitation','Proctoring','Lab'] as const;

export default function ManageWorkload() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [availableTAs, setAvailableTAs] = useState<TA[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [current, setCurrent] = useState<Partial<Task>>({
    title: '', date: '', time: '', type: 'Recitation', status: 'pending', assignedId: ''
  });

  // 1) Seed mock tasks on mount
  useEffect(() => {
    setTasks(initialTasks);
  }, []);

  // 2) Fetch TA list once on mount
  const [loading, setLoading] = useState(true);
// filepath: frontend/src/components/ManageWorkload.tsx
useEffect(() => {
  console.log('⏳ Fetching TAs…')
  fetchAllTAs()
    .then(res => {
      console.log('✅ fetchAllTAs response:', res.data)
      setAvailableTAs(res.data)
    })
    .catch(err => {
      console.error('❌ fetchAllTAs error:', err)
    })
    .finally(() => {
      setLoading(false)
    })
}, [])

  // 3) Open modal for add
  const openAdd = () => {
    setCurrent({ title:'', date:'', time:'', type:'Recitation', status:'pending', assignedId: '' });
    setIsEdit(false);
    setModalOpen(true);
  };

  // 4) Open modal for edit
  const openEdit = (t: Task) => {
    setCurrent({ ...t });
    setIsEdit(true);
    setModalOpen(true);
  };

  // 5) Handle form changes
  const handleChange = (e: ChangeEvent<HTMLInputElement|HTMLSelectElement>) => {
    const { name, value } = e.target;
    setCurrent(c => ({ ...c, [name]: value }));
  };

  // 6) Save add/edit
  const handleSave = (e: FormEvent) => {
    e.preventDefault();
    if (!current.title || !current.date || !current.time || !current.type) return;

    if (isEdit && current.id != null) {
      setTasks(ts =>
        ts.map(t =>
          t.id === current.id
            ? { ...t, ...current } as Task
            : t
        )
      );
    } else {
      const nextId = Math.max(0, ...tasks.map(t => t.id)) + 1;
      setTasks(ts => [
        ...ts,
        { id: nextId, status: 'pending', ...current } as Task
      ]);
    }
    setModalOpen(false);
  };

  // 7) Delete
  const handleDelete = (id: number) => {
    setTasks(ts => ts.filter(t => t.id !== id));
  };

  return (
    <div className={styles.pageWrapper}>
      

      <h1 className={styles.heading}>Manage Workload</h1>
      <button className={styles.addBtn} onClick={openAdd}>+ Add Task</button>

{/*      {loading
  ? <div>Loading TAs…</div>
  : availableTAs.length === 0
}  */}
      <table className={styles.table}>
        <thead>
          <tr>
            <th>Title</th><th>Date</th><th>Time</th>
            <th>Type</th><th>Status</th><th>Assigned TA</th><th>Actions</th>
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
                {availableTAs.find(a => a.id === t.assignedId)?.displayName || '—'}
              </td>
              <td>
                <button className={styles.updateBtn} onClick={() => openEdit(t)}>Update</button>
                <button className={styles.deleteBtn} onClick={() => handleDelete(t.id)}>Delete</button>
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
              <input name="title" value={current.title||''} onChange={handleChange} required />

              <label>Date</label>
              <input name="date" type="date" value={current.date||''} onChange={handleChange} required />

              <label>Time</label>
              <input name="time" type="time" value={current.time||''} onChange={handleChange} required />

              <label>Type</label>
              <select name="type" value={current.type||''} onChange={handleChange}>
                {TASK_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>

              <label>Status</label>
              <select name="status" value={current.status||''} onChange={handleChange}>
                <option value="pending">pending</option>
                <option value="approved">approved</option>
              </select>

              <label>Assign TA</label>
              <select name="assignedId" value={current.assignedId||''} onChange={handleChange}>
                <option value="">— none —</option>
                {availableTAs.map(a=>(
                  <option key={a.id} value={a.id}>{a.displayName}</option>
                ))}
              </select>

              <div className={styles.modalBtns}>
                <button type="button" onClick={() => setModalOpen(false)}>Cancel</button>
                <button type="submit">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
