// src/pages/ManageWorkload/ManageWorkload.tsx
import React, { useState, ChangeEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import InsNavBar from '../../components/NavBars/InsNavBar';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp, { ErrorPopupProps } from '../../components/PopUp/ErrPopUp';
import styles from './ManageWorkload.module.css';

interface Task {
  id: number;
  type: 'Lab' | 'Grading' | 'Recitation';
  date: string;
  startTime?: number;
  endTime?: number;
  gradingEndTime?: string;
  /** list of already assigned TA names */
  alreadySelectedTAs: string[];
}

const startTimes = ['08:30','09:30','10:30','11:30','13:30','14:30','15:30','16:30'];
const endTimes   = ['09:20','10:20','11:20','12:20','14:20','15:20','16:20','17:20'];

const initialTasks: Task[] = [
  { id: 1, type: 'Lab',        date: '2025-05-05', startTime: 2, endTime: 2, alreadySelectedTAs: ['Ali Veli'] },
  { id: 2, type: 'Grading',    date: '2025-05-10', gradingEndTime: '17:20',    alreadySelectedTAs: [] },
  { id: 3, type: 'Recitation', date: '2025-05-06', startTime: 1, endTime: 1, alreadySelectedTAs: ['Mehmet Can','Ayşe Fatma'] },
];

type RawConfirm = { action: 'delete' | 'deleteAll' | 'save'; id?: number };

const ManageWorkload: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const courseCode = location.pathname.split('/')[2] || 'Unknown Course';

  const [tasks, setTasks] = useState<Task[]>(initialTasks);
  const [modalOpen, setModalOpen] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [current, setCurrent] = useState<Partial<Task>>({
    type: 'Lab',
    date: new Date().toISOString().slice(0,10),
    startTime: 1,
    endTime: 1,
    gradingEndTime: endTimes[0],
    alreadySelectedTAs: []
  });

  const [confirm, setConfirm] = useState<RawConfirm|null>(null);
  const [errorPopup, setErrorPopup] = useState<ErrorPopupProps|null>(null);
  const [deleteTarget, setDeleteTarget] = useState<number|null>(null);

  const showError = (msg: string) =>
    setErrorPopup({ message: msg, onConfirm: () => setErrorPopup(null) });

  // CRUD helpers
  const deleteTask = (id: number) => setTasks(ts => ts.filter(t => t.id !== id));
  const deleteAllTasks = () => setTasks([]);

  // open Add/Edit modal
  const openAdd = () => {
    setCurrent({
      type: 'Lab',
      date: new Date().toISOString().slice(0,10),
      startTime: 1,
      endTime: 1,
      gradingEndTime: endTimes[0],
      alreadySelectedTAs: []
    });
    setIsEdit(false);
    setModalOpen(true);
  };
  const openEdit = (t: Task) => {
    setCurrent({ ...t });
    setIsEdit(true);
    setModalOpen(true);
  };

  // form field changes
  const handleChange = (e: ChangeEvent<HTMLSelectElement|HTMLInputElement>) => {
    const { name, value } = e.target;
    setCurrent(prev => {
      if (name === 'type') {
        return {
          type: value as Task['type'],
          date: new Date().toISOString().slice(0,10),
          startTime:1,
          endTime:1,
          gradingEndTime: endTimes[0],
          alreadySelectedTAs: prev.alreadySelectedTAs || []
        };
      }
      if (name === 'date') {
        return { ...prev, date: value };
      }
      if (name === 'startTime' || name === 'endTime') {
        return { ...prev, [name]: Number(value) };
      }
      if (name === 'gradingEndTime') {
        return { ...prev, gradingEndTime: value };
      }
      return prev;
    });
  };

  // validate & save
  const validate = () => {
    if (current.type !== 'Grading') {
      if (!current.date || !current.startTime || !current.endTime) {
        showError('Please fill date, start and end time.');
        return false;
      }
      if (current.endTime! < current.startTime!) {
        showError('End time must be after start time.');
        return false;
      }
    } else {
      if (!current.gradingEndTime) {
        showError('Please select an end time for grading.');
        return false;
      }
    }
    return true;
  };

  const save = () => {
    if (!validate()) return;
    const newTask: Task = {
      id: isEdit ? current.id! : Math.max(0, ...tasks.map(t => t.id)) + 1,
      type: current.type as Task['type'],
      date: current.date!,
      startTime: current.type !== 'Grading' ? current.startTime : undefined,
      endTime: current.type !== 'Grading' ? current.endTime : undefined,
      gradingEndTime: current.type === 'Grading' ? current.gradingEndTime : undefined,
      alreadySelectedTAs: current.alreadySelectedTAs ?? []
    };
    setTasks(prev =>
      isEdit
        ? prev.map(t => t.id === newTask.id ? newTask : t)
        : [...prev, newTask]
    );
    setModalOpen(false);
  };

  return (
    <div className={styles.pageWrapper}>
      

      <div className={styles.headerRow}>
        <BackBut to="/ins"/>
        <h1 className={styles.title}>{courseCode} Workload</h1>
      </div>

      <div className={styles.content} style={{ filter: modalOpen||!!confirm ? 'blur(4px)' : 'none' }}>
        <table className={styles.taskTable}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Type</th>
              <th>Date</th>
              <th>Start</th>
              <th>End</th>
              <th>Assigned</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {tasks.map(t => (
              <tr key={t.id}>
                <td>{t.id}</td>
                <td>{t.type}</td>
                <td>{t.date}</td>
                <td>{t.type === 'Grading' ? '—' : startTimes[t.startTime! - 1]}</td>
                <td>{t.type === 'Grading' ? t.gradingEndTime : endTimes[t.endTime! - 1]}</td>
                <td>{t.alreadySelectedTAs.length}</td>
                <td className={styles.actionsCell}>
                  <button
                    className={styles.deleteBtn}
                    onClick={() => {
                      setDeleteTarget(t.id);
                      setConfirm({ action: 'delete', id: t.id });
                    }}
                  >
                    Delete
                  </button>
                  <button className={styles.updateBtn} onClick={() => openEdit(t)}>
                    Update
                  </button>
                  <button
                    className={styles.assignBtn}
                    onClick={() => navigate(`/man/${courseCode}/${t.id}`)}
                  >
                    Assign TA
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className={styles.headerRow2}>
          <button
            className={styles.deleteAllBtn}
            onClick={() => {
              if (tasks.length) {
                setConfirm({ action: 'deleteAll' });
              } else {
                showError('No tasks to delete.');
              }
            }}
          >
            Delete All Tasks
          </button>
          <button className={styles.addBtn} onClick={openAdd}>
            Add Task
          </button>
        </div>
      </div>

      {/* Modals & Popups */}
      {modalOpen && (
        <div className={styles.modalOverlay}>
          {/* ...modal form as before... */}
          {/* omitted for brevity */}
        </div>
      )}
      {confirm?.action === 'delete' && (
        <ConPop
          message="Delete this task?"
          onConfirm={() => { deleteTask(deleteTarget!); setConfirm(null); }}
          onCancel={() => setConfirm(null)}
        />
      )}
      {confirm?.action === 'deleteAll' && (
        <ConPop
          message="Delete all tasks?"
          onConfirm={() => { deleteAllTasks(); setConfirm(null); }}
          onCancel={() => setConfirm(null)}
        />
      )}
      {confirm?.action === 'save' && (
        <ConPop
          message="Confirm save?"
          onConfirm={() => { save(); setConfirm(null); }}
          onCancel={() => setConfirm(null)}
        />
      )}
      {errorPopup && <ErrPopUp {...errorPopup} />}
    </div>
  );
};

export default ManageWorkload;
