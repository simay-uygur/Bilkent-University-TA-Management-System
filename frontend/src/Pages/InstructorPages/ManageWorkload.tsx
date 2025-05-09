// src/pages/ManageWorkload/ManageWorkload.tsx
import React, { useState, useEffect, ChangeEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp, { ErrorPopupProps } from '../../components/PopUp/ErrPopUp';
import styles from './ManageWorkload.module.css';

interface TimePart {
  year: number;
  month: number;
  day: number;
  hour: number;
  minute: number;
}

interface Duration {
  start: TimePart;
  finish: TimePart;
  ongoing: boolean;
}

interface TaskDto {
  taskId: number;
  type: 'Lab' | 'Recitation' | 'Grading';
  tas: any[];                   // replace `any` with your TA type if available
  description: string | null;
  duration: Duration;
  status: string;
  workload: number;
}

type RawConfirm = { action: 'delete' | 'deleteAll' | 'save'; id?: number };

const startTimes = ['08:30','09:30','10:30','11:30','13:30','14:30','15:30','16:30'];
const endTimes   = ['09:20','10:20','11:20','12:20','14:20','15:20','16:20','17:20'];

const ManageWorkload: React.FC = () => {
  const { sectionCode } = useParams<{ sectionCode: string }>();
  const navigate = useNavigate();

  // parse header parts
  const parts = sectionCode?.split('-') || [];
  const courseCode    = parts.length >= 2 ? `${parts[0]}-${parts[1]}` : sectionCode || '';
  const courseSection = parts.length >= 3 ? parts[2] : '1';

  // component state
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [currentId, setCurrentId] = useState<number|undefined>(undefined);
  const [currentType, setCurrentType] = useState<TaskDto['type']>('Lab');
  const [currentDescription, setCurrentDescription] = useState('');
  const [currentDate, setCurrentDate] = useState(() => new Date().toISOString().slice(0,10));
  const [currentStartTime, setCurrentStartTime] = useState(1);
  const [currentEndTime,   setCurrentEndTime]   = useState(1);
  const [currentGradingEndTime, setCurrentGradingEndTime] = useState(endTimes[0]);
  const [confirm, setConfirm] = useState<RawConfirm|null>(null);
  const [errorPopup, setErrorPopup] = useState<ErrorPopupProps|null>(null);
  const [deleteTarget, setDeleteTarget] = useState<number|null>(null);

  const showError = (msg: string) =>
    setErrorPopup({ message: msg, onConfirm: () => setErrorPopup(null) });

  const pad = (n: number) => n.toString().padStart(2, '0');
  const formatTime = (tp: TimePart) => `${pad(tp.hour)}:${pad(tp.minute)}`;

  // build Duration from form fields
  const buildDuration = (): Duration => {
    const [year, month, day] = currentDate.split('-').map(Number);
    // parse selected startTime slot into hour/minute
    const [sh, sm] = startTimes[currentStartTime - 1].split(':').map(Number);
    let fh: number, fm: number;
    if (currentType !== 'Grading') {
      [fh, fm] = endTimes[currentEndTime - 1].split(':').map(Number);
    } else {
      [fh, fm] = currentGradingEndTime.split(':').map(Number);
    }
    return {
      start:  { year, month, day, hour: sh, minute: sm },
      finish: { year, month, day, hour: fh, minute: fm },
      ongoing: false
    };
  };

  // ——— Load tasks ———
  const loadTasks = async () => {
    try {
      const res = await fetch(`/api/sections/section/${sectionCode}/task`);
      if (!res.ok) throw new Error();
      const data: TaskDto[] = await res.json();
      setTasks(data);
    } catch {
      showError('Failed to load tasks.');
    }
  };
  useEffect(() => { loadTasks() }, [sectionCode]);

  // ——— Delete one ———
  const deleteTask = async (id: number) => {
    try {
      const res = await fetch(
        `/api/sections/section/${sectionCode}/task/${id}`,
        { method: 'DELETE' }
      );
      if (!res.ok) throw new Error();
      setTasks(ts => ts.filter(t => t.taskId !== id));
    } catch {
      showError('Failed to delete task.');
    }
  };

  // ——— Delete all ———
  const deleteAllTasks = async () => {
    try {
      const res = await fetch(
        `/api/sections/section/${sectionCode}/task`,
        { method: 'DELETE' }
      );
      if (!res.ok) throw new Error();
      setTasks([]);
    } catch {
      showError('Failed to delete all tasks.');
    }
  };

  // ——— Open modals ———
  const openAdd = () => {
    setCurrentId(undefined);
    setCurrentType('Lab');
    setCurrentDescription('');
    setCurrentDate(new Date().toISOString().slice(0,10));
    setCurrentStartTime(1);
    setCurrentEndTime(1);
    setCurrentGradingEndTime(endTimes[0]);
    setIsEdit(false);
    setModalOpen(true);
  };

  const openEdit = (t: TaskDto) => {
    setCurrentId(t.taskId);
    setCurrentType(t.type);
    setCurrentDescription(t.description ?? '');
    // derive date and slot indexes from t.duration
    const { start, finish } = t.duration;
    setCurrentDate(`${start.year}-${pad(start.month)}-${pad(start.day)}`);
    const sLabel = `${pad(start.hour)}:${pad(start.minute)}`;
    const fLabel = t.type !== 'Grading'
      ? `${pad(finish.hour)}:${pad(finish.minute)}`
      : `${pad(finish.hour)}:${pad(finish.minute)}`;
    setCurrentStartTime(Math.max(1, startTimes.indexOf(sLabel) + 1));
    setCurrentEndTime(Math.max(1, endTimes.indexOf(fLabel) + 1));
    setCurrentGradingEndTime(fLabel);
    setIsEdit(true);
    setModalOpen(true);
  };

  // ——— Field changes ———
  const handleChange = (e: ChangeEvent<HTMLInputElement|HTMLSelectElement>) => {
    const { name, value } = e.target;
    switch (name) {
      case 'type':
        setCurrentType(value as TaskDto['type']);
        break;
      case 'description':
        setCurrentDescription(value);
        break;
      case 'date':
        setCurrentDate(value);
        break;
      case 'startTime':
        setCurrentStartTime(Number(value));
        break;
      case 'endTime':
        setCurrentEndTime(Number(value));
        break;
      case 'gradingEndTime':
        setCurrentGradingEndTime(value);
        break;
    }
  };

<<<<<<< Updated upstream
  // ——— Save (POST/PUT) ———
  const save = async () => {
    if (!currentDescription.trim()) {
      showError('Description required.');
      return;
    }
    if (currentType !== 'Grading') {
      const [sh, sm] = startTimes[currentStartTime - 1].split(':').map(Number);
      const [fh, fm] = endTimes[currentEndTime - 1].split(':').map(Number);
      const startM = sh * 60 + sm;
      const endM   = fh * 60 + fm;
      if (endM <= startM) {
        showError('End must be after start.');
        return;
      }
      const lunchStart = 12*60+30, lunchEnd = 13*60+30;
      if (startM < lunchEnd && endM > lunchStart) {
        showError('Cannot overlap 12:30–13:30.');
        return;
=======
  // validate & save
  const validate = () => {
    if (current.type !== 'Grading') {
      // helper to turn a slot index into an “HH:MM” string
const startLabel = startTimes[current.startTime! - 1];
const endLabel   = endTimes[current.endTime! - 1];

// 1) Make sure end > start
if (current.endTime! < current.startTime!) {
  showError('End time must be after start time.');
  return false;
}
    // 2) Ban any overlap with 12:30–13:30
    function toMins(t: string) {
      const [h, m] = t.split(':').map(Number);
      return h * 60 + m;
    }
    const taskStart = toMins(startLabel);
    const taskEnd   = toMins(endLabel);
    const lunchStart = toMins('12:30');
    const lunchEnd   = toMins('13:30');

    if (taskStart < lunchEnd && taskEnd > lunchStart) {
      showError('Lab and Recitation may not overlap the 12:30–13:30 break.');
      return false;
    }
    } else {
      if (!current.gradingEndTime) {
        showError('Please select an end time for grading.');
        return false;
>>>>>>> Stashed changes
      }
    }

    const payload = {
      type: currentType,
      description: currentDescription.trim() === '' ? courseCode + currentType : currentDescription.trim(),
      duration: buildDuration()
    };

    try {
      const res = await fetch(
        `/api/sections/section/${sectionCode}/task${isEdit && currentId ? `/${currentId}` : ''}`,
        {
          method: isEdit ? 'PUT' : 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        }
      );
      if (!res.ok) throw new Error();
      await loadTasks();
      setModalOpen(false);
    } catch {
      showError('Failed to save task.');
    }
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/instructor" />
        <h1 className={styles.title}>
          Workload of {courseCode}<br/>Section {courseSection}
        </h1>
      </div>

      <div className={styles.content} style={{ filter: modalOpen||!!confirm ? 'blur(4px)' : 'none' }}>
        <table className={styles.taskTable}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Type</th>
              <th>Description</th>
              <th>Start</th>
              <th>Finish</th>
              <th>Assigned</th>
              <th>Workload</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {tasks.map(t => (
              <tr key={t.taskId}>
                <td>{t.taskId}</td>
                <td>{t.type}</td>
                <td>{t.description || 'Empty'}</td>
                <td>{formatTime(t.duration.start)}</td>
                <td>{formatTime(t.duration.finish)}</td>
                {/* inside your <tbody> map: */}
<td>
  <div className={styles.taTooltip}>
    {t.tas.length === 0
      ? <span className={styles.noAssigned}>No</span>
      : <span className={styles.yesAssigned}>Yes ({t.tas.length})</span>
    }
    <div className={styles.tooltipContent}>
      {t.tas.map(ta => (
        <div key={ta.id} className={styles.tooltipItem}>
          {ta.name}
        </div>
      ))}
    </div>
  </div>
</td>
                <td>{t.workload}</td>
                <td>{t.status}</td>
                <td className={styles.actionsCell}>
                  <button
                    className={styles.deleteBtn}
                    onClick={() => {
                      setDeleteTarget(t.taskId);
                      setConfirm({ action: 'delete', id: t.taskId });
                    }}
                  >
                    Delete
                  </button>
                  <button
                    className={styles.updateBtn}
                    onClick={() => openEdit(t)}
                  >
                    Update
                  </button>
                  <button
                    className={styles.assignBtn}
                    onClick={() => navigate(`/instructor/workload/${sectionCode}/${t.taskId}`)}
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
              if (tasks.length) setConfirm({ action: 'deleteAll' });
              else showError('No tasks to delete.');
            }}
          >
            Delete All Tasks
          </button>
          <button className={styles.addBtn} onClick={openAdd}>
            Add Task
          </button>
        </div>
      </div>

      {modalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h2>{isEdit ? 'Edit Task' : 'Add Task'}</h2>
            <form>
              <div className={styles.fieldRow}>
                <label htmlFor="type">Type</label>
                <select
                  id="type" name="type"
                  value={currentType}
                  onChange={handleChange}
                >
                  <option value="Lab">Lab</option>
                  <option value="Recitation">Recitation</option>
                  <option value="Grading">Grading</option>
                </select>
              </div>

              <div className={styles.fieldRow}>
                <label htmlFor="description">Description</label>
                <input
                  id="description" name="description" type="text"
                  value={currentDescription}
                  onChange={handleChange}
                />
              </div>

              <div className={styles.fieldRow}>
                <label htmlFor="date">Date</label>
                <input
                  type="date"
                  id="date" name="date"
                  value={currentDate}
                  onChange={handleChange}
                />
              </div>

              {currentType !== 'Grading' ? (
                <>
                  <div className={styles.fieldRow}>
                    <label htmlFor="startTime">Start Time</label>
                    <select
                      id="startTime" name="startTime"
                      value={currentStartTime}
                      onChange={handleChange}
                    >
                      {startTimes.map((t, i) => (
                        <option key={t} value={i+1}>{t}</option>
                      ))}
                    </select>
                  </div>
                  <div className={styles.fieldRow}>
                    <label htmlFor="endTime">End Time</label>
                    <select
                      id="endTime" name="endTime"
                      value={currentEndTime}
                      onChange={handleChange}
                    >
                      {endTimes.map((t, i) => (
                        <option key={t} value={i+1}>{t}</option>
                      ))}
                    </select>
                  </div>
                </>
              ) : (
                <div className={styles.fieldRow}>
                  <label htmlFor="gradingEndTime">Grading Ends At</label>
                  <select
                    id="gradingEndTime" name="gradingEndTime"
                    value={currentGradingEndTime}
                    onChange={handleChange}
                  >
                    {endTimes.map(t => (
                      <option key={t} value={t}>{t}</option>
                    ))}
                  </select>
                </div>
              )}

              <div className={styles.buttonsRow}>
                <button type="button" onClick={() => setConfirm({ action: 'save' })}>
                  {isEdit ? 'Update' : 'Save'}
                </button>
                <button type="button" onClick={() => setModalOpen(false)}>
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

/* // src/pages/ManageWorkload/ManageWorkload.tsx
import React, { useState, useEffect, ChangeEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp, { ErrorPopupProps } from '../../components/PopUp/ErrPopUp';
import styles from './ManageWorkload.module.css';

interface TimePart {
  year: number;
  month: number;
  day: number;
  hour: number;
  minute: number;
}

interface Duration {
  start: TimePart;
  finish: TimePart;
}

interface TaskDto {
  id: number;
  type: 'Lab' | 'Recitation' | 'Grading';
  duration: Duration;
  description: string;
}

type RawConfirm = { action: 'delete' | 'deleteAll' | 'save'; id?: number };

const ManageWorkload: React.FC = () => {
  const { sectionCode } = useParams<{ sectionCode: string }>();
  const navigate = useNavigate();

  const parts = sectionCode?.split('-') || [];
  const courseCode = parts.length >= 2 ? `${parts[0]}-${parts[1]}` : sectionCode || '';
  const courseSection = parts.length >= 3 ? parts[2] : '1';

  // --- State now holds TaskDto[]
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [currentId, setCurrentId] = useState<number|undefined>(undefined);
  const [currentType, setCurrentType] = useState<TaskDto['type']>('Lab');
  const [currentDescription, setCurrentDescription] = useState('');
  const [currentDate, setCurrentDate] = useState(() => new Date().toISOString().slice(0,10));
  const [startHour, setStartHour] = useState(8);
  const [startMinute, setStartMinute] = useState(30);
  const [finishHour, setFinishHour] = useState(9);
  const [finishMinute, setFinishMinute] = useState(20);
  const [confirm, setConfirm] = useState<RawConfirm|null>(null);
  const [errorPopup, setErrorPopup] = useState<ErrorPopupProps|null>(null);
  const [deleteTarget, setDeleteTarget] = useState<number|null>(null);

  const showError = (msg: string) =>
    setErrorPopup({ message: msg, onConfirm: () => setErrorPopup(null) });

  const pad = (n: number) => n.toString().padStart(2,'0');

  const formatTime = (tp: TimePart) =>
    `${pad(tp.hour)}:${pad(tp.minute)}`;

  // build a Duration from our form fields
  const buildDuration = (): Duration => {
    const [year, month, day] = currentDate.split('-').map(Number);
    return {
      start: { year, month, day, hour: startHour, minute: startMinute },
      finish:{ year, month, day, hour: finishHour, minute: finishMinute }
    };
  };

  // — load from GET …
  const loadTasks = async () => {
    try {
      const res = await fetch(`/api/sections/section/${sectionCode}/task`);
      if (!res.ok) throw new Error();
      const data: TaskDto[] = await res.json();
      setTasks(data);
    } catch {
      showError('Failed to load tasks.');
    }
  };
  useEffect(() => { loadTasks() }, [sectionCode]);

  // — delete one —
  const deleteTask = async (id: number) => {
    try {
      const res = await fetch(`/api/sections/section/${sectionCode}/task/${id}`, {
        method: 'DELETE'
      });
      if (!res.ok) throw new Error();
      setTasks(ts => ts.filter(t => t.id !== id));
    } catch {
      showError('Failed to delete task.');
    }
  };

  // — delete all —
  const deleteAllTasks = async () => {
    try {
      const res = await fetch(`/api/sections/section/${sectionCode}/task`, {
        method: 'DELETE'
      });
      if (!res.ok) throw new Error();
      setTasks([]);
    } catch {
      showError('Failed to delete all tasks.');
    }
  };

  // — open modal for add vs edit —
  const openAdd = () => {
    setCurrentId(undefined);
    setCurrentType('Lab');
    setCurrentDescription('');
    setCurrentDate(new Date().toISOString().slice(0,10));
    setStartHour(8); setStartMinute(30);
    setFinishHour(9); setFinishMinute(20);
    setIsEdit(false);
    setModalOpen(true);
  };
  const openEdit = (t: TaskDto) => {
    setCurrentId(t.id);
    setCurrentType(t.type);
    setCurrentDescription(t.description);
    // fill date + times from the DTO
    const { start, finish } = t.duration;
    setCurrentDate(`${start.year}-${pad(start.month)}-${pad(start.day)}`);
    setStartHour(start.hour); setStartMinute(start.minute);
    setFinishHour(finish.hour); setFinishMinute(finish.minute);
    setIsEdit(true);
    setModalOpen(true);
  };

  // — save (POST or PUT) —
  const save = async () => {
    if (!currentDescription.trim()) {
      showError('Description required.');
      return;
    }
    // basic time validation for Lab/Recitation
    if (currentType !== 'Grading') {
      const startM = startHour*60 + startMinute;
      const endM   = finishHour*60 + finishMinute;
      if (endM <= startM) {
        showError('End must be after start.');
        return;
      }
      // lunch check…
      const lunchS = 12*60 + 30, lunchE = 13*60 + 30;
      if (startM < lunchE && endM > lunchS) {
        showError('Cannot overlap 12:30–13:30.');
        return;
      }
    }

    const payload = {
      type: currentType,
      description: currentDescription,
      duration: buildDuration()
    };

    try {
      const res = await fetch(
        `/api/sections/section/${sectionCode}/task${isEdit?`/${currentId}`:''}`,
        {
          method: isEdit ? 'PUT' : 'POST',
          headers: { 'Content-Type':'application/json' },
          body: JSON.stringify(payload)
        }
      );
      if (!res.ok) throw new Error();
      await loadTasks();
      setModalOpen(false);
    } catch {
      showError('Failed to save task.');
    }
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/instructor"/>
        <h1 className={styles.title}>
          Workload of {courseCode}<br/>Section {courseSection}
        </h1>
      </div>

      <div className={styles.content} style={{ filter: modalOpen||!!confirm ? 'blur(4px)' : 'none' }}>
        <table className={styles.taskTable}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Type</th>
              <th>Course Section</th>
              <th>Start</th>
              <th>Finish</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {tasks.map(t => (
              <tr key={t.id}>
                <td>{t.id}</td>
                <td>{t.type}</td>
                <td>{t.description}</td>
                <td>{formatTime(t.duration.start)}</td>
                <td>{formatTime(t.duration.finish)}</td>
                <td className={styles.actionsCell}>
                  <button
                    className={styles.deleteBtn}
                    onClick={() => {
                      setDeleteTarget(t.id);
                      setConfirm({ action: 'delete', id: t.id });
                    }}
                  >Delete</button>
                  <button
                    className={styles.updateBtn}
                    onClick={() => openEdit(t)}
                  >Update</button>
                  <button
                    className={styles.assignBtn}
                    onClick={() => navigate(`/instructor/workload/${sectionCode}/${t.id}`)}
                  >Assign TA</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className={styles.headerRow2}>
          <button
            className={styles.deleteAllBtn}
            onClick={() => {
              if (tasks.length) setConfirm({ action: 'deleteAll' });
              else showError('No tasks to delete.');
            }}
          >Delete All Tasks</button>
          <button className={styles.addBtn} onClick={openAdd}>Add Task</button>
        </div>
      </div>

      {modalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h2>{isEdit ? 'Edit Task' : 'Add Task'}</h2>
            <form>
              <div className={styles.fieldRow}>
                <label htmlFor="type">Type</label>
                <select
                  id="type" name="type"
                  value={currentType}
                  onChange={e => setCurrentType(e.target.value as any)}
                >
                  <option value="Lab">Lab</option>
                  <option value="Recitation">Recitation</option>
                  <option value="Grading">Grading</option>
                </select>
              </div>

              <div className={styles.fieldRow}>
                <label htmlFor="description">Description</label>
                <input
                  id="description" name="description" type="text"
                  value={currentDescription}
                  onChange={e => setCurrentDescription(e.target.value)}
                />
              </div>

              <div className={styles.fieldRow}>
                <label htmlFor="date">Date</label>
                <input
                  type="date"
                  id="date" name="date"
                  value={currentDate}
                  onChange={e => setCurrentDate(e.target.value)}
                />
              </div>

              <div className={styles.fieldRow}>
                <label>Start Time</label>
                <input
                  type="number"
                  value={startHour}
                  onChange={e => setStartHour(+e.target.value)}
                />
                <input
                  type="number"
                  value={startMinute}
                  onChange={e => setStartMinute(+e.target.value)}
                />
              </div>

              <div className={styles.fieldRow}>
                <label>Finish Time</label>
                <input
                  type="number"
                  value={finishHour}
                  onChange={e => setFinishHour(+e.target.value)}
                />
                <input
                  type="number"
                  value={finishMinute}
                  onChange={e => setFinishMinute(+e.target.value)}
                />
              </div>

              <div className={styles.buttonsRow}>
                <button
                  type="button"
                  onClick={() => setConfirm({ action: 'save' })}
                >
                  {isEdit ? 'Update' : 'Save'}
                </button>
                <button type="button" onClick={() => setModalOpen(false)}>
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
 */