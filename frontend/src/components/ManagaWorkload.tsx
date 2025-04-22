// src/components/ManageWorkload.tsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  createTask,
  fetchAllTasks,
  fetchAssignedTAs,
  Task
} from '../api';
import styles from './ManageWorkload.module.css';

const ManageWorkload: React.FC = () => {
  const { courseId } = useParams<{ courseId: string }>();
  const [tasks, setTasks]         = useState<Task[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [newTitle, setNewTitle]   = useState('');
  const [newAssigned, setNewAssigned] = useState<string>('');

  // Load existing tasks and their assigned TAs
  useEffect(() => {
    if (!courseId) return;
    fetchAllTasks()
      .then(res => res.data.filter(t => t.courseId === +courseId))
      .then(courseTasks =>
        Promise.all(
          courseTasks.map(async t => {
            const taRes = await fetchAssignedTAs(t.id);
            return { ...t, assignedTAs: taRes.data };
          })
        )
      )
      .then(fullTasks => setTasks(fullTasks))
      .catch(console.error);
  }, [courseId]);

  // Handle form submit inside the modal
  const handleCreateTask = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim() || !courseId) return;

    createTask({ title: newTitle.trim(), courseId: +courseId })
      .then(res => {
        setTasks(prev => [...prev, { ...res.data, assignedTAs: [] }]);
        setNewTitle('');
        setNewAssigned('');
        setShowModal(false);
      })
      .catch(console.error);
  };

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>
        Manage Workload for Course {courseId}
      </h1>

      <button
        className={styles.addBtn}
        onClick={() => setShowModal(true)}
      >
        + Add Task
      </button>

      {showModal && (
        <div
          className={styles.modalBackdrop}
          onClick={() => setShowModal(false)}
        >
          <div
            className={styles.modalCard}
            onClick={e => e.stopPropagation()}
          >
            <form onSubmit={handleCreateTask}>
              <h2 className={styles.modalTitle}>Create New Task</h2>

              <label className={styles.label}>Task Title</label>
              <input
                type="text"
                value={newTitle}
                onChange={e => setNewTitle(e.target.value)}
                className={styles.input}
              />

              <label className={styles.label}>
                Assign TA (optional)
              </label>
              <select
                value={newAssigned}
                onChange={e => setNewAssigned(e.target.value)}
                className={styles.select}
              >
                <option value="">-- None --</option>
                <option value="ta1">Ali Veli</option>
                <option value="ta2">Ayşe Yılmaz</option>
                <option value="ta3">Mehmet Demir</option>
              </select>

              <button
                type="submit"
                className={styles.submitBtn}
              >
                Create Task
              </button>
            </form>
          </div>
        </div>
      )}

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Task</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map(task => (
            <tr key={task.id}>
              <td>{task.title}</td>
              <td>{task.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ManageWorkload;
