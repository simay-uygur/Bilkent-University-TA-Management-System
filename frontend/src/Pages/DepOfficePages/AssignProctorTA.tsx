// src/pages/AssignProctorTA.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import LoadingPage from '../CommonPages/LoadingPage';
import styles from './AssignProctorTA.module.css';
import BackBut from '../../components/Buttons/BackBut';

interface AvailableTA {
  workload: number;
  hasAdjacentExam: boolean;
  taId: number;
  name: string;
  surname: string;
  academicLevel: 'BS' | 'MS' | 'PHD';
}

interface TA {
  id: number;
  name: string;
  level: string;
  workload: number;
  hasAdjacentExam: boolean;
  selected: boolean;
}

interface ExamDetailResponse {
  examId: number;
  duration: { start: DateInfo; finish: DateInfo; ongoing: boolean; };
  courseCode: string;
  type: string;
  examRooms: string[];
  requiredTas: number;
  workload: number;
}

interface DateInfo {
  day: number; month: number; year: number; hour: number; minute: number;
}

interface Exam {
  id: number;
  courseCode: string;
  courseName: string;
  examType: string;
  date: string;
  startTime: string;
  endTime: string;
  rooms: string[];
  requiredTAs: number;
  workload: number;
  availableTAs: TA[];
  selectedTAs: TA[];
}

const AssignProctorTA: React.FC = () => {
  const navigate = useNavigate();
  const { examId, requestId } = useParams<{ examId: string; requestId: string }>();

  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState<string|null>(null);
  const [exam, setExam]       = useState<Exam|null>(null);
  const [confirmMsg, setConfirmMsg] = useState<string|null>(null);
  const [errorMsg, setErrorMsg]     = useState<string|null>(null);

  useEffect(() => {
    const fetchExamData = async () => {
      if (!examId) {
        setError('No exam ID provided');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);

        // **your existing courseCode lookup, unchanged**
        let examCourseCode: string;
        try {
          const courseResp = await axios.get(`/api/exam/${examId}/course`);
          examCourseCode = courseResp.data.courseCode;
        } catch {
          examCourseCode = 'CS-464';
        }

        // **your existing parallel fetches, unchanged**
        const [detailsRes, availRes] = await Promise.all([
          axios.get<ExamDetailResponse>(`/api/instructors/${examCourseCode}/exams/${examId}`),
          axios.get<AvailableTA[]>(`/api/course/${examCourseCode}/proctoring/exam/${examId}`)
        ]);

        const d = detailsRes.data;
        const a = availRes.data;

        // format date/time
        const { start, finish } = d.duration;
        const date      = `${start.year}-${String(start.month).padStart(2,'0')}-${String(start.day).padStart(2,'0')}`;
        const startTime = `${String(start.hour).padStart(2,'0')}:${String(start.minute).padStart(2,'0')}`;
        const endTime   = `${String(finish.hour).padStart(2,'0')}:${String(finish.minute).padStart(2,'0')}`;

        // map into our TA shape
        const formattedAvailable: TA[] = a.map(ta => ({
          id: ta.taId,
          name: `${ta.name} ${ta.surname}`,
          level: ta.academicLevel,
          workload: ta.workload,
          hasAdjacentExam: ta.hasAdjacentExam,
          selected: false
        }));

        // —— NEW: auto-select lowest-workload TAs up to requiredTAs —— 
        const sorted = [...formattedAvailable].sort((x,y)=> x.workload - y.workload);
        const toAutoSelect = sorted.slice(0, d.requiredTas).map(x => x.id);
        const withFlags = formattedAvailable.map(x => ({
          ...x,
          selected: toAutoSelect.includes(x.id)
        }));
        const initialSelected = withFlags.filter(x => x.selected);

        // build our Exam object
        const formattedExam: Exam = {
          id: d.examId,
          courseCode: d.courseCode,
          courseName: d.courseCode,
          examType: d.type,
          date, startTime, endTime,
          rooms: d.examRooms,
          requiredTAs: d.requiredTas,
          workload: d.workload,
          availableTAs: withFlags,
          selectedTAs: initialSelected
        };
        // —— end auto-select logic —— 

        setExam(formattedExam);
      } catch (err) {
        console.error(err);
        setError('Failed to load exam data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchExamData();
  }, [examId]);

  // toggle handler remains the same
  const handleTASelection = (taId: number) => {
    if (!exam) return;
    const isSelected = exam.selectedTAs.some(t => t.id === taId);

    // remove or add
    const newSelected = isSelected
      ? exam.selectedTAs.filter(t => t.id !== taId)
      : [...exam.selectedTAs, { ...exam.availableTAs.find(t=>t.id===taId)!, selected:true }];

    const newAvailable = exam.availableTAs.map(t =>
      t.id === taId ? { ...t, selected: !isSelected } : t
    );

    setExam({ ...exam, selectedTAs: newSelected, availableTAs: newAvailable });
  };

  const handleSaveAssignments = async () => {
    if (!exam || !requestId) return;
    const approverId = localStorage.getItem('userId');
    if (!approverId) {
      setErrorMsg('User not authenticated.');
      return;
    }
    if (exam.selectedTAs.length === 0) {
      setErrorMsg('Select at least one TA.');
      return;
    }

    try {
      await axios.post(`/api/ta/${approverId}/departmentproctor/${requestId}/assign`, {
        taIds: exam.selectedTAs.map(t=>t.id),
        examId: exam.id
      });
      setConfirmMsg('TA assignments saved successfully!');
      setTimeout(() => navigate('/department-office/assign-proctor'), 1500);
    } catch {
      setErrorMsg('Failed to save assignments.');
    }
  };

  if (loading) return <LoadingPage/>;
  if (error || !exam) return (
    <div className={styles.errorContainer}>
      <h2>Error</h2>
      <p>{error || 'Unknown error'}</p>
      <button onClick={()=>window.location.reload()}>Retry</button>
      <button onClick={()=>navigate('/department-office/assign-proctor')}>Back</button>
    </div>
  );

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office/assign-proctor" />
        <h1 className={styles.title}>
          Assign TAs for {exam.courseName} {exam.examType}
        </h1>
      </div>

      <div className={styles.examInfoContainer}>
        <p><strong>Date:</strong> {exam.date}</p>
        <p><strong>Time:</strong> {exam.startTime} - {exam.endTime}</p>
        <p><strong>Rooms:</strong> {exam.rooms.join(', ')}</p>
        <p><strong>Required TAs:</strong> {exam.requiredTAs}</p>
        <p><strong>Workload:</strong> {exam.workload} hours</p>
      </div>

      <div className={styles.mainContent}>
        <div className={styles.availableTAsContainer}>
          <h2>Available TAs</h2>
          <ul className={styles.taList}>
            {exam.availableTAs.map(ta => (
              <li
                key={ta.id}
                className={`${styles.taItem} ${ta.selected ? styles.selected : ''} ${ta.hasAdjacentExam ? styles.warning : ''}`}
                onClick={()=>handleTASelection(ta.id)}
              >
                <div className={styles.taName}>{ta.name}</div>
                <div className={styles.taInfo}>
                  <span>{ta.level}</span>
                  <span>WL: {ta.workload}</span>
                  {ta.hasAdjacentExam && <span title="Adjacent exam">⚠️</span>}
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div className={styles.selectedTAsContainer}>
          <h2>Selected TAs ({exam.selectedTAs.length}/{exam.requiredTAs})</h2>
          <ul className={styles.taList}>
            {exam.selectedTAs.map(ta => (
              <li
                key={ta.id}
                className={`${styles.taItem} ${styles.selected}`}
                onClick={()=>handleTASelection(ta.id)}
              >
                <div className={styles.taName}>{ta.name}</div>
                <div className={styles.taInfo}>
                  <span>{ta.level}</span>
                  <span>WL: {ta.workload}</span>
                  {ta.hasAdjacentExam && <span title="Adjacent exam">⚠️</span>}
                </div>
              </li>
            ))}
            {exam.selectedTAs.length === 0 && <p className={styles.noTAs}>No TAs selected</p>}
          </ul>
        </div>
      </div>

      <div className={styles.buttonContainer}>
        <button onClick={()=>navigate('/department-office/assign-proctor')} className={styles.cancelButton}>
          Cancel
        </button>
        <button
          onClick={handleSaveAssignments}
          disabled={exam.selectedTAs.length === 0}
          className={styles.saveButton}
        >
          Save Assignments
        </button>
      </div>

      {confirmMsg && <div className={styles.confirmMessage}>{confirmMsg}</div>}
      {errorMsg   && <ErrPopUp message={errorMsg} onConfirm={()=>setErrorMsg(null)} />}
    </div>
  );
};

export default AssignProctorTA;
