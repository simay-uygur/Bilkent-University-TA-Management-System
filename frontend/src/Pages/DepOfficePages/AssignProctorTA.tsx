import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import LoadingPage from '../CommonPages/LoadingPage';
import BackBut from '../../components/Buttons/BackBut';
import styles from './AssignProctorTA.module.css';

interface DateInfo { day: number; month: number; year: number; hour: number; minute: number; }
interface AvailableTA { taId: number; name: string; surname: string; academicLevel: 'BS' | 'MS' | 'PHD'; workload: number; hasAdjacentExam: boolean; }
interface TA { id: number; name: string; level: string; workload: number; hasAdjacentExam: boolean; selected: boolean; }
interface ExamDetailResponse { examId: number; duration: { start: DateInfo; finish: DateInfo; ongoing: boolean }; courseCode: string; type: string; examRooms: string[]; requiredTas: number; workload: number; }
interface Exam { id: number; courseCode: string; examType: string; date: string; startTime: string; endTime: string; rooms: string[]; requiredTAs: number; workload: number; availableTAs: TA[]; selectedTAs: TA[]; }

const AssignProctorTA: React.FC = () => {
  const navigate = useNavigate();
  const { examId, requestId } = useParams<{ examId: string; requestId: string }>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [exam, setExam] = useState<Exam | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [confirmMsg, setConfirmMsg] = useState<string | null>(null);
  const [restrictToBS, setRestrictToBS] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      if (!examId) { setError('No exam ID'); setLoading(false); return; }
      try {
        setLoading(true);
        let courseCode: string;
        try {
          const r = await axios.get<{ courseCode: string }>(`/api/exam/${examId}/course`);
          courseCode = r.data.courseCode;
        } catch {
          courseCode = 'CS-464';
        }
        const [dRes, aRes] = await Promise.all([
          axios.get<ExamDetailResponse>(`/api/instructors/${courseCode}/exams/${examId}`),
          axios.get<AvailableTA[]>(`/api/course/${courseCode}/proctoring/exam/${examId}`)
        ]);
        const d = dRes.data;
        const { start, finish } = d.duration;
        const date = `${start.year}-${String(start.month).padStart(2,'0')}-${String(start.day).padStart(2,'0')}`;
        const startTime = `${String(start.hour).padStart(2,'0')}:${String(start.minute).padStart(2,'0')}`;
        const endTime = `${String(finish.hour).padStart(2,'0')}:${String(finish.minute).padStart(2,'0')}`;
        const availableTAs = aRes.data.map(ta => ({
          id: ta.taId,
          name: `${ta.name} ${ta.surname}`,
          level: ta.academicLevel,
          workload: ta.workload,
          hasAdjacentExam: ta.hasAdjacentExam,
          selected: false
        }));
        const sorted = [...availableTAs].sort((x,y) => x.workload - y.workload);
        const pickIds = sorted.slice(0, d.requiredTas).map(t => t.id);
        const withSelection = availableTAs.map(t => ({ ...t, selected: pickIds.includes(t.id) }));
        setExam({
          id: d.examId,
          courseCode: d.courseCode,
          examType: d.type,
          date, startTime, endTime,
          rooms: d.examRooms,
          requiredTAs: d.requiredTas,
          workload: d.workload,
          availableTAs: withSelection,
          selectedTAs: withSelection.filter(t => t.selected)
        });
      } catch (e) {
        console.error(e);
        setError('Failed to load exam data.');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [examId]);

  const handleTASelection = (id: number) => {
    if (!exam) return;
    const isSelected = exam.selectedTAs.some(t => t.id === id);
    if (!isSelected && exam.selectedTAs.length >= exam.requiredTAs) {
      setErrorMsg(`Cannot select more than ${exam.requiredTAs} TAs.`);
      return;
    }
    const newAvail = exam.availableTAs.map(t => t.id === id ? { ...t, selected: !isSelected } : t);
    const newSel = isSelected
      ? exam.selectedTAs.filter(t => t.id !== id)
      : [...exam.selectedTAs, newAvail.find(t => t.id === id)!];
    setExam({ ...exam, availableTAs: newAvail, selectedTAs: newSel });
    setErrorMsg(null);
  };

  const handleAutoAssign = () => {
    if (!exam) return;
    const pool = restrictToBS ? exam.availableTAs.filter(t => t.level === 'BS') : exam.availableTAs;
    const sorted = [...pool].sort((a,b) => a.workload - b.workload);
    const pick = sorted.slice(0, exam.requiredTAs).map(t => t.id);
    const newAvail = exam.availableTAs.map(t => ({ ...t, selected: pick.includes(t.id) }));
    setExam({ ...exam, availableTAs: newAvail, selectedTAs: newAvail.filter(t => t.selected) });
    setErrorMsg(null);
  };

  const handleManualAssign = () => {
    if (!exam) return;
    const cleared = exam.availableTAs.map(t => ({ ...t, selected: false }));
    setExam({ ...exam, availableTAs: cleared, selectedTAs: [] });
    setErrorMsg(null);
  };

  const handleBreakConsecutive = async () => {
    if (!exam) return;
    const dept = exam.courseCode.split('-')[0];
    try {
      const res = await axios.get<AvailableTA[]>(`/api/ta/department/${dept}`);
      const extras = res.data.map(ta => ({ id: ta.taId, name: `${ta.name} ${ta.surname}`, level: ta.academicLevel, workload: ta.workload, hasAdjacentExam: ta.hasAdjacentExam, selected: false }));
      setExam(prev => prev && { ...prev, availableTAs: [...prev.availableTAs, ...extras] });
    } catch (e) {
      console.error(e);
      setErrorMsg('Failed to fetch department TAs.');
    }
  };

  const handleToggleRestriction = () => {
    setRestrictToBS(prev => !prev);
    setErrorMsg(null);
  };

  const handleSaveAssignments = async () => {
    if (!exam || !requestId) return;
    const approver = localStorage.getItem('userId');
    if (!approver) { setErrorMsg('User not authenticated.'); return; }
    if (exam.selectedTAs.length === 0) { setErrorMsg('Select at least one TA.'); return; }
    try {
      await axios.post(`/api/v1/offerings/course/${exam.courseCode}/exam/${exam.id}/tas`, exam.selectedTAs.map(t => t.id));
      setConfirmMsg('TA assignments saved!');
      setTimeout(() => navigate('/department-office/assign-proctor'), 1500);
    } catch {
      setErrorMsg('Failed to save assignments.');
    }
  };

  if (loading) return <LoadingPage />;
  if (error || !exam) return (
    <div className={styles.errorContainer}>
      <h2>Error</h2><p>{error || 'Unknown error'}</p>
      <button onClick={() => window.location.reload()}>Retry</button>
      <button onClick={() => navigate('/department-office/assign-proctor')}>Back</button>
    </div>
  );

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office/assign-proctor" />
        <h1 className={styles.title}>Assign TAs for {exam.courseCode} {exam.examType}</h1>
      </div>
      <div className={styles.examInfoContainer}>
        <p><strong>Date:</strong> {exam.date}</p>
        <p><strong>Time:</strong> {exam.startTime} – {exam.endTime}</p>
        <p><strong>Rooms:</strong> {exam.rooms.join(', ')}</p>
        <p><strong>Required TAs:</strong> {exam.requiredTAs}</p>
        <p><strong>Workload:</strong> {exam.workload}h</p>
      </div>
      <div className={styles.mainContent}>
        <div className={styles.availableTAsContainer}>
          <h2>Available TAs</h2>
          <ul className={styles.taList}>
            {exam.availableTAs.map(ta => (
              <li
                key={ta.id}
                className={`${styles.taItem} ${ta.selected ? styles.selected : ''} ${ta.hasAdjacentExam ? styles.warning : ''}`}
                onClick={() => handleTASelection(ta.id)}
              >
                <div className={styles.taName}>{ta.name}</div>
                <div className={styles.taInfo}>
                  <span>{ta.level}</span>
                  <span>WL: {ta.workload}</span>
                  {ta.hasAdjacentExam && <span title="⚠️" role="img">⚠️</span>}
                </div>
              </li>
            ))}
          </ul>
        </div>
        <div className={styles.selectedTAsContainer}>
          <h2>Selected TAs ({exam.selectedTAs.length}/{exam.requiredTAs})</h2>
          <ul className={styles.taList}>
            {exam.selectedTAs.length > 0 ? exam.selectedTAs.map(ta => (
              <li key={ta.id} className={`${styles.taItem} ${styles.selected}`} onClick={() => handleTASelection(ta.id)}>
                <div className={styles.taName}>{ta.name}</div>
                <div className={styles.taInfo}>
                  <span>{ta.level}</span>
                  <span>WL: {ta.workload}</span>
                  {ta.hasAdjacentExam && <span title="⚠️" role="img">⚠️</span>}
                </div>
              </li>
            )) : <p className={styles.noTAs}>No TAs selected</p>}
          </ul>
        </div>
      </div>
      <div className={styles.buttonContainer}>
        <button onClick={handleAutoAssign} className={styles.autoButton}>Assign Automatically</button>
        <button onClick={handleManualAssign} className={styles.manualButton}>Assign Manually</button>
        <button onClick={handleBreakConsecutive} className={styles.breakButton}>Break Consecutive Days</button>
        <button onClick={handleToggleRestriction} className={styles.toggleButton}>{restrictToBS ? 'Allow All Levels' : 'Restrict to BS'}</button>
        <button onClick={() => navigate('/department-office/assign-proctor')} className={styles.cancelButton}>Cancel</button>
        <button onClick={handleSaveAssignments} disabled={exam.selectedTAs.length === 0} className={styles.saveButton}>Save Assignments</button>
      </div>
      {confirmMsg && <div className={styles.confirmMessage}>{confirmMsg}</div>}
      {errorMsg && <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />}    
    </div>
  );
};

export default AssignProctorTA;
