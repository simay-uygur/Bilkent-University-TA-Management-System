import React, { useState, useEffect, useMemo, useRef } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import BackBut from '../../components/Buttons/BackBut';
import GreenBut from '../../components/Buttons/GreenBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import LoadingPage from '../CommonPages/LoadingPage';
import styles from './AssignProctorTA.module.css';

// --- API DTOs
interface ProctorRequestInfo {
  requestId: number;
  examId: number;
  examName: string;
  courseName: string;
  courseCode: string;
  requiredTas: number;
  tasLeft: number;
  assignedTas: TA[];
}

interface AvailableTA {
  taId: number;
  name: string;
  surname: string;
  academicLevel: string;
  workload: number;
  hasAdjacentExam: boolean;
}

// --- UI models
export interface TA {
  id: string | number;
  name: string;
  level: string;
  workload: number;
  hasAdjacentExam: boolean;
}

const AssignProctorTA: React.FC = () => {
  const navigate = useNavigate();
  const { examID } = useParams<{ examID: string }>();
  const [searchParams] = useSearchParams();
  const requestId = searchParams.get('request');

  // Global state
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [examData, setExamData] = useState<ProctorRequestInfo | null>(null);
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  // Confirm dialogs
  const [showExitConfirm, setShowExitConfirm] = useState(false);
  const [showSaveConfirm, setShowSaveConfirm] = useState(false);

  // Track initial assigned list to detect unsaved changes
  const initialAssigned = useRef<(string|number)[]>([]);


  // Fetch exam info and available TAs
  useEffect(() => {
    const fetchData = async () => {
      if (!requestId || !examID) {
        setError('Missing request or exam ID');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        // --- fetch request info ---
        const reqResp = await axios.get<ProctorRequestInfo>(
            `/api/request/${requestId}`
        );
        const info = reqResp.data;
        setExamData(info);

        // --- transform assigned TAs ---
        setAssigned(info.assignedTas);
        initialAssigned.current = info.assignedTas.map(t => t.id);

        // --- fetch available TAs ---

        const taResp = await axios.get<AvailableTA[]>(
            `/api/course/${info.courseCode}/proctoring/exam/${info.examId}`
        );
        const allTAs: TA[] = taResp.data.map(t => ({
          id: t.taId,
          name: `${t.name} ${t.surname}`,
          level: t.academicLevel,
          workload: t.workload,
          hasAdjacentExam: t.hasAdjacentExam,
        }));

        // filter out already assigned
        const assignedSet = new Set(info.assignedTas.map(t => t.id));
        setPotential(allTAs.filter(t => !assignedSet.has(t.id)));
      } catch (err: any) {
        console.error('Failed to load data', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [requestId, examID]);

  const filtered = useMemo(() =>
          potential
              .filter(t => t.name.toLowerCase().includes(searchTerm.toLowerCase()))
              .sort((a,b)=>a.workload - b.workload)
      , [potential, searchTerm]);

  // if fetch complete but no data
  if (!loading && !examData) {
    return (
        <div className={styles.pageWrapper}>
          <p>Exam not found.</p>
          <button onClick={() => navigate('/department-office/assign-proctor')}>Back</button>
        </div>
    );
  }

  const leftCount = examData ? Math.max(0, examData.requiredTas - assigned.length) : 0;

  const toggle = (ta: TA) => {
    if (assigned.find(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else if (examData && assigned.length < examData.requiredTas) {
      setAssigned(a => [...a, ta]);
      setPotential(p => p.filter(x => x.id !== ta.id));
    } else {
      setErrorMsg(`Cannot assign more than ${examData?.requiredTas} TAs.`);
    }
  };

  // navigation guards
  const handleBack = () => {
    const curr = assigned.map(t=>t.id).sort().join();
    const init = initialAssigned.current.sort().join();
    if (curr !== init) setShowExitConfirm(true);
    else navigate('/department-office/assign-proctor');
  };
  const confirmExit = () => navigate('/department-office/assign-proctor');

  // save assignments
  const handleSave = () => setShowSaveConfirm(true);
  const confirmSave = async () => {
    if (!requestId || !examData) return;
    try {
      setSaving(true);
      await axios.post(`/api/proctor-request/${requestId}/assign-tas`, {
        examId: examData.examId,
        taIds: assigned.map(t=>t.id)
      });
      initialAssigned.current = assigned.map(t=>t.id);
      setErrorMsg('Assignments saved');
      setShowSaveConfirm(false);
      setTimeout(() => navigate('/department-office/assign-proctor'), 1000);
    } catch (e) {
      console.error('Save failed', e);
      setErrorMsg('Save failed, try again');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingPage />;
  if (error) return <div className={styles.error}>{error}</div>;

  return (
      <div className={styles.pageWrapper}>
        <div className={styles.header}>
          <BackBut onClick={handleBack} />
          <h1>{examData!.courseName} – {examData!.examName}</h1>
          <div>Needed: {examData!.requiredTas}  Left: {leftCount}</div>
        </div>

        <input
            className={styles.search}
            placeholder="Search TAs…"
            value={searchTerm}
            onChange={e=>setSearchTerm(e.target.value)}
        />

        <table className={styles.table}>
          <thead><tr>
            <th>Name</th><th>Level</th><th>Workload</th><th>Adjacent?</th>
          </tr></thead>
          <tbody>
          {filtered.map(ta => (
              <tr key={ta.id}
                  className={assigned.find(a=>a.id===ta.id)?styles.selected:''}
                  onClick={()=>toggle(ta)}>
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
                <td>{ta.hasAdjacentExam?'Yes':'No'}</td>
              </tr>
          ))}
          {!filtered.length && <tr><td colSpan={4}>No matching TAs</td></tr>}
          </tbody>
        </table>

        <div className={styles.selectedSection}>
          <h2>Assigned ({assigned.length})</h2>
          <ul>
            {assigned.map(ta=>(
                <li key={ta.id} onClick={()=>toggle(ta)}>
                  {ta.name} ×
                </li>
            ))}
          </ul>
        </div>

        <GreenBut text="Save Changes" onClick={handleSave} />

        {errorMsg && <ErrPopUp message={errorMsg} onConfirm={()=>setErrorMsg(null)} />}

        {showExitConfirm && (
            <ConPop
                message="Unsaved changes will be lost. Continue?"
                onConfirm={confirmExit}
                onCancel={()=>setShowExitConfirm(false)}
            />
        )}

        {showSaveConfirm && (
            <ConPop
                message="Save TA assignments?"
                onConfirm={confirmSave}
                onCancel={()=>setShowSaveConfirm(false)}
            />
        )}
      </div>
  );
};

export default AssignProctorTA;

/*
import React, { useState, useMemo, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import GreenBut from '../../components/Buttons/GreenBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignProctorTA.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  type: 'Full-time' | 'Part-time';
}

export interface no{
  fullCap: number;
  partCap: number;
}

const no: no = {
  fullCap: 20,
  partCap: 10,
}

export interface Exam {
  id: string;
  courseName: string;
  examType: 'Midterm' | 'Final';
  needed: number;
  tasLeft: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
}

const sampleExams: Exam[] = [
  {
    id: 'e1',
    courseName: 'Algorithms – Midterm',
    examType: 'Midterm',
    needed: 3,
    tasLeft: 1,
    assignedTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, type: 'Full-time' },
      { id: 'ta2', name: 'Ayşe Fatma', level: 'MS',  workload: 4, hasAdjacentExam: true,  type: 'Part-time' },
    ],
    potentialTAs: [
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, type: 'Full-time' },
      { id: 'ta4', name: 'John Smith', level: 'BS',  workload: 3, hasAdjacentExam: true,  type: 'Part-time' },
    ],
  },
  {
    id: 'e2',
    courseName: 'Data Structures – Final',
    examType: 'Final',
    needed: 2,
    tasLeft: 2,
    assignedTAs: [],
    potentialTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, type: 'Full-time' },
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, type: 'Full-time' },
    ],
  },
];

const AssignProctorTA: React.FC = () => {
  const navigate = useNavigate();
  const { examID } = useParams<{ examID: string }>();
  const examData = sampleExams.find(e => e.id === examID);
  if (!examData) {
    return <div className={styles.pageWrapper}><p className={styles.notFound}>Exam not found.</p></div>;
  }

  const { needed } = examData;
  const [assigned, setAssigned] = useState<TA[]>([...examData.assignedTAs]);
  const [potential, setPotential] = useState<TA[]>([...examData.potentialTAs]);
  const [searchTerm, setSearchTerm] = useState('');
  const [errorMsg, setErrorMsg] = useState<string|null>(null);

  // for exit and save confirmations
  const [showExitConfirm, setShowExitConfirm] = useState(false);
  const [showSaveConfirm, setShowSaveConfirm] = useState(false);

  // track initial assigned IDs
  const initialAssigned = useRef<string[]>(examData.assignedTAs.map(t => t.id));

  const leftCount = Math.max(0, needed - assigned.length);

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else if (assigned.length < needed) {
      setAssigned(a => [...a, ta]);
      setPotential(p => p.filter(x => x.id !== ta.id));
    } else {
      setErrorMsg(`Cannot select more than ${needed} TAs.`);
    }
  };

  const filtered = useMemo(() => {
    return potential
      .filter(t => t.name.toLowerCase().includes(searchTerm.toLowerCase()))
      .sort((a,b)=> a.workload - b.workload);
  }, [potential, searchTerm]);

  const handleBackClick = () => {
    const curr = assigned.map(t => t.id).sort().join();
    const init = initialAssigned.current.sort().join();
    if (curr !== init) {
      setShowExitConfirm(true);
    } else {
      navigate('/department-office/assign-proctor');
    }
  };

  const onConfirmExit = () => {
    setShowExitConfirm(false);
    // TODO: persist or discard as needed
    navigate('/department-office/assign-proctor');
  };

  const onCancelExit = () => {
    setShowExitConfirm(false);
  };

  const handleSaveClick = () => {
    setShowSaveConfirm(true);
  };

  const onConfirmSave = () => {
    // TODO: persist assigned changes to backend
    initialAssigned.current = assigned.map(t => t.id);
    setShowSaveConfirm(false);
    navigate('/department-office/assign-proctor');
  };

  const onCancelSave = () => {
    setShowSaveConfirm(false);
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={handleBackClick} />
        <h1 className={styles.title}>{examData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </div>

      <div className={styles.search}>
        <input
          type="text"
          placeholder="Search by name…"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
        />
      </div>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
            <th>Adjacent?</th>
            <th>Type</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(ta => (
            <tr
              key={ta.id}
              className={assigned.some(a => a.id === ta.id) ? styles.selectedRow : ''}
              onClick={() => toggleSelect(ta)}
            >
              <td>{ta.name}</td>
              <td>{ta.level}</td>
              <td>{ta.workload}</td>
              <td>{ta.hasAdjacentExam ? 'Yes' : 'No'}</td>
              <td>{ta.type}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => (
            <li key={ta.id} onClick={() => toggleSelect(ta)}>
              {ta.name} ×
            </li>
          ))}
        </ul>
      </div>

      <div className={styles.actions}>
        <GreenBut text="Save Changes" onClick={handleSaveClick} />
      </div>

      <div className={styles.info}>
        Full Time Workload Capacity: {no.fullCap} <br />
        Part Time Workload Capacity: {no.partCap} <br />
      </div>

      {errorMsg && <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />}

      {showSaveConfirm && (
        <ConPop
          message="Do you want to save the changes you made?"
          onConfirm={onConfirmSave}
          onCancel={onCancelSave}
        />
      )}

      {showExitConfirm && (
        <ConPop
          message="You have unsaved changes. Are you sure you want to leave?"
          onConfirm={onConfirmExit}
          onCancel={onCancelExit}
        />
      )}
    </div>
  );
};

export default AssignProctorTA;
 */