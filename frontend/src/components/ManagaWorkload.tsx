import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  fetchAllTasks,
  fetchAssignedTAs,
  fetchAllTAs,
  createTask,
  assignTA,
  approveTask,
  rejectTask,
  Task,
  TA
} from '../api';
import styles from './ManageWorkload.module.css';

const TASK_TYPES = ['Citation', 'Proctoring', 'Lab'] as const;

export default function ManageWorkload() {
  const { courseId } = useParams<{ courseId: string }>();
  const [tasks, setTasks] = useState<(Task & { assignedTAs: TA[] })[]>([]);
  const [availableTAs, setAvailableTAs] = useState<TA[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [title, setTitle] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [type, setType] = useState<typeof TASK_TYPES[number]>('Citation');
  const [assigned, setAssigned] = useState<string>('');

  // 1) load all TAs once
  useEffect(() => {
    fetchAllTAs()
      .then(res => setAvailableTAs(res.data))
      .catch(err => console.error('Error loading TAs:', err));
  }, []);

  // 2) extract task loader
  const loadTasks = () => {
    if (!courseId) return;
    fetchAllTasks()
      .then(res => res.data.filter(t => t.courseId === +courseId))
      .then(ts => Promise.all(
        ts.map(async t => {
          const taRes = await fetchAssignedTAs(t.id);
          return { ...t, assignedTAs: taRes.data };
        })
      ))
      .then(full => setTasks(full))
      .catch(err => console.error('Error loading tasks:', err));
  };

  // 3) fetch tasks on courseId change
  useEffect(loadTasks, [courseId]);

  // 4) handle create + reload
  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title || !date || !time) return;
    try {
      const res = await createTask({
        title,
        courseId: +courseId!,
        date,
        time,
        type,
        assignedId: assigned || undefined
      });
      console.log('Task created:', res.data);
      if (assigned) {
        await assignTA(res.data.id, assigned);
        console.log(`Assigned TA ${assigned} to task ${res.data.id}`);
      }
      loadTasks();
      setShowModal(false);
      setTitle(''); setDate(''); setTime(''); setType('Citation'); setAssigned('');
    } catch (err) {
      console.error('Error creating task:', err);
    }
  };

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>Manage Workload for Course {courseId}</h1>
      <button className={styles.addBtn} onClick={() => setShowModal(true)}>
        + Add Task
      </button>

      {showModal && (
        <div className={styles.modalBackdrop} onClick={() => setShowModal(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <h2 className={styles.modalTitle}>Create Task</h2>
            <form onSubmit={handleCreate}>
              <label>Title</label>
              <input type="text" value={title} onChange={e => setTitle(e.target.value)} />

              <label>Date</label>
              <input type="date" value={date} onChange={e => setDate(e.target.value)} />

              <label>Time</label>
              <input type="time" value={time} onChange={e => setTime(e.target.value)} />

              <label>Type</label>
              <select value={type} onChange={e => setType(e.target.value as any)}>
                {TASK_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>

              <label>Assign TA</label>
              <select value={assigned} onChange={e => setAssigned(e.target.value)}>
                <option value="">-- none --</option>
                {availableTAs.map(ta => (
                  <option key={ta.id} value={ta.id}>
                    {ta.displayName}
                  </option>
                ))}
              </select>

              <button type="submit">Create</button>
            </form>
          </div>
        </div>
      )}

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Title</th><th>Date</th><th>Time</th><th>Type</th>
            <th>Assigned</th><th>Status</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map(task => (
            <tr key={task.id}>
              <td>{task.title}</td>
              <td>{task.date}</td>
              <td>{task.time}</td>
              <td>{task.type}</td>
              <td>{task.assignedTAs.map(t => t.displayName).join(', ') || '—'}</td>
              <td>{task.status}</td>
              <td>
                {task.status === 'pending' && (
                  <>
                    <button onClick={() => approveTask(task.id).then(loadTasks)}>
                      Approve
                    </button>
                    <button onClick={() => rejectTask(task.id).then(loadTasks)}>
                      Reject
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}