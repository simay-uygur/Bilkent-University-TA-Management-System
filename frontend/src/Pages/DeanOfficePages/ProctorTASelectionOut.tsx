/* eslint-disable react-hooks/rules-of-hooks */
/* src/pages/AssignTA/ProctorTASelectionOut.tsx */
import React, { useState, useMemo, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import FacultyFilter from './FacultyFilter';
import GreenBut from '../../components/Buttons/GreenBut';
import styles from './ProctorTASelectionOut.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  type: 'Full-time' | 'Part-time';
  faculty: string;
  department: string;
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
      { id: 'ta1', name: 'Ali Veli', level: 'BS', workload: 2, hasAdjacentExam: false, type: 'Full-time', faculty: 'Engineering', department: 'CS' },
      { id: 'ta2', name: 'Ayşe Fatma', level: 'MS', workload: 4, hasAdjacentExam: true, type: 'Part-time', faculty: 'Engineering', department: 'EE' },
    ],
    potentialTAs: [
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, type: 'Full-time', faculty: 'Science', department: 'Math' },
      { id: 'ta4', name: 'John Smith', level: 'BS', workload: 3, hasAdjacentExam: true, type: 'Part-time', faculty: 'Engineering', department: 'CS' },
    ],
  },
];

const ProctorTASelectionOut: React.FC = () => {
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
  const [selectedFaculty, setSelectedFaculty] = useState('All');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [showExitConfirm, setShowExitConfirm] = useState(false);
  const [showSaveConfirm, setShowSaveConfirm] = useState(false);

  // track initial assigned TAs for change detection
  const initialAssigned = useRef<string[]>(examData.assignedTAs.map(t => t.id));
  const leftCount = Math.max(0, needed - assigned.length);

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(prev => prev.filter(x => x.id !== ta.id));
      setPotential(prev => [...prev, ta]);
    } else {
      if (assigned.length >= needed) {
        setErrorMsg(`Cannot select more than ${needed} TAs.`);
        return;
      }
      setAssigned(prev => [...prev, ta]);
      setPotential(prev => prev.filter(x => x.id !== ta.id));
    }
  };

  // options for faculty filter
  const faculties = useMemo(() => {
    const setFac = new Set(examData.potentialTAs.map(t => t.faculty));
    return ['All', ...Array.from(setFac)];
  }, [examData]);

  const filtered = useMemo(() => {
    return potential
      .filter(t => selectedFaculty === 'All' || t.faculty === selectedFaculty)
      .filter(t => t.name.toLowerCase().includes(searchTerm.toLowerCase()))
      .sort((a, b) => a.workload - b.workload);
  }, [potential, searchTerm, selectedFaculty]);

  const handleBackClick = () => {
    const curr = assigned.map(t => t.id).sort().join();
    const init = initialAssigned.current.sort().join();
    if (curr !== init) setShowExitConfirm(true);
    else navigate('/deans-office/proctor-out');
  };

  const onConfirmExit = () => {
    setShowExitConfirm(false);
    // optionally discard unsaved changes
    navigate('/deans-office/proctor-out');
  };

  const onCancelExit = () => {
    setShowExitConfirm(false);
  };

  const handleSaveClick = () => {
    setShowSaveConfirm(true);
  };

  const onConfirmSave = () => {
    // TODO: persist assigned TAs to backend
    initialAssigned.current = assigned.map(t => t.id);
    setShowSaveConfirm(false);
    navigate('/deans-office/proctor-out');
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

      <div className={styles.filters}>
        <FacultyFilter
          faculties={faculties}
          selected={selectedFaculty}
          onChange={setSelectedFaculty}
        />
        <input
          className={styles.search}
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
            <th>Faculty</th>
            <th>Department</th>
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
              <td>{ta.faculty}</td>
              <td>{ta.department}</td>
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
          message="You have unsaved changes. Are you sure you want to leave without saving?"
          onConfirm={onConfirmExit}
          onCancel={onCancelExit}
        />
      )}
    </div>
  );
};

export default ProctorTASelectionOut;