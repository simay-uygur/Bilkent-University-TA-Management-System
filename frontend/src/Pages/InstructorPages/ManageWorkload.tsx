// src/pages/ManageWorkload/ManageWorkload.tsx
import React, { useState, ChangeEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
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
// Get the section code from URL params
const { sectionCode } = useParams<{ sectionCode: string }>();
const navigate = useNavigate();

// Parse the section code format CS-319-1-2025-SPRING
const parts = sectionCode?.split('-') || [];
const courseCode = parts.length >= 2 ? `${parts[0]}-${parts[1]}` : sectionCode || '';
const courseSection = parts.length >= 3 ? parts[2] : '1';
  
 /*  // If courseSec is undefined, we might have the full section code in courseID
  if (!courseSec && courseID && courseID.split('-').length >= 3) {
    const parts = courseID.split('-');
    courseCode = `${parts[0]}-${parts[1]}`; // e.g., "CS-464"
    courseSection = parts[2];                // e.g., "1"
  } */
  
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
        <BackBut to="/instructor"/>
        <h1 className={styles.title}>Workload of {courseCode} <br/> Section {courseSection}
        </h1>
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
                    onClick={() => navigate(`/instructor/workload/${sectionCode}/${t.id}`)}
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
          <div className={styles.modal}>
            <h2>{isEdit ? 'Edit Task' : 'Add Task'}</h2>
            <form>
              <div className={styles.fieldRow}>
                <label htmlFor="type">Type</label>
                <select
                  id="type"
                  name="type"
                  value={current.type}
                  onChange={handleChange}
                >
                  <option value="Lab">Lab</option>
                  <option value="Grading">Grading</option>
                  <option value="Recitation">Recitation</option>
                </select>
              </div>

              <div className={styles.fieldRow}>
                <label htmlFor="date">Date</label>
                <input
                  type="date"
                  id="date"
                  name="date"
                  value={current.date}
                  onChange={handleChange}
                />
              </div>

              {current.type !== 'Grading' ? (
                <>
                  <div className={styles.fieldRow}>
                    <label htmlFor="startTime">Start Time</label>
                    <select
                      id="startTime"
                      name="startTime"
                      value={current.startTime}
                      onChange={handleChange}
                    >
                      {startTimes.map((t, i) => (
                        <option key={t} value={i + 1}>
                          {t}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className={styles.fieldRow}>
                    <label htmlFor="endTime">End Time</label>
                    <select
                      id="endTime"
                      name="endTime"
                      value={current.endTime}
                      onChange={handleChange}
                    >
                      {endTimes.map((t, i) => (
                        <option key={t} value={i + 1}>
                          {t}
                        </option>
                      ))}
                    </select>
                  </div>
                </>
              ) : (
                <div className={styles.fieldRow}>
                  <label htmlFor="gradingEndTime">Grading Ends At</label>
                  <select
                    id="gradingEndTime"
                    name="gradingEndTime"
                    value={current.gradingEndTime}
                    onChange={handleChange}
                  >
                    {endTimes.map((t) => (
                      <option key={t} value={t}>
                        {t}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div className={styles.buttonsRow}>
                <button
                  type="button"
                  onClick={() => setConfirm({ action: 'save' })}
                >
                  {isEdit ? 'Update' : 'Save'}
                </button>
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
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
