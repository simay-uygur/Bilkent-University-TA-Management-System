// src/pages/AssignTA/AssignTA.tsx
import React, { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import DepOfNavBar from '../../components/NavBars/DepOfNavBar';
import BackBut from '../../components/Buttons/BackBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import { sampleExams } from './data';
import styles from './AssignTA.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
}

const levelScore = { BS: 1, MS: 2, PhD: 3 } as const;
const examReqScore = { BS: 1, MS: 2, PhD: 3 } as const;

const AssignTA: React.FC = () => {
  const navigate = useNavigate();
  const { examId } = useParams<{ examId: string }>();
  const examData = sampleExams.find(e => e.id === examId);
  if (!examData) {
    return (
      <div className={styles.pageWrapper}>
        <DepOfNavBar />
        <p className={styles.notFound}>Exam not found.</p>
      </div>
    );
  }

  const needed = examData.needed;
  const [assigned, setAssigned] = useState<TA[]>([...examData.assignedTAs]);
  const [potential, setPotential] = useState<TA[]>([...examData.potentialTAs]);

  const [restrictLevel, setRestrictLevel] = useState(false);
  const [excludeAdjacent, setExcludeAdjacent] = useState(false);
  const [enforceMax, setEnforceMax] = useState(false);
  const [maxWorkload, setMaxWorkload] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const [autoMsg, setAutoMsg] = useState<string | null>(null);
  const [showAutoErr, setShowAutoErr] = useState(false);

  const leftCount = Math.max(0, needed - assigned.length);
  examData.tasLeft = leftCount;

  const toggleSelect = (ta: TA) => {
    const inAssigned = assigned.some(a => a.id === ta.id);
    if (inAssigned) {
      const newAssigned = assigned.filter(a => a.id !== ta.id);
      const newPotential = [...potential, ta];
      setAssigned(newAssigned);
      setPotential(newPotential);
      examData.assignedTAs = newAssigned;
      examData.potentialTAs = newPotential;
    } else {
      if (assigned.length >= needed) {
        setErrorMsg(`Cannot select more than ${needed} TAs.`);
        return;
      }
      const newAssigned = [...assigned, ta];
      const newPotential = potential.filter(a => a.id !== ta.id);
      setAssigned(newAssigned);
      setPotential(newPotential);
      examData.assignedTAs = newAssigned;
      examData.potentialTAs = newPotential;
    }
    examData.tasLeft = needed - examData.assignedTAs.length;
  };

  const filterPredicate = (ta: TA) => {
    if (restrictLevel && levelScore[ta.level] < examReqScore[examData.level]) return false;
    if (excludeAdjacent && ta.hasAdjacentExam) return false;
    if (enforceMax && ta.workload > maxWorkload) return false;
    return true;
  };

  const filtered = useMemo(() => {
    let list = potential;
    if (searchTerm) {
      list = list.filter(t => t.name.toLowerCase().includes(searchTerm.toLowerCase()));
    }
    return list.filter(filterPredicate).sort((a, b) => a.workload - b.workload);
  }, [potential, searchTerm, restrictLevel, excludeAdjacent, enforceMax, maxWorkload]);

  const handleConfirm = () => {
    setShowConfirm(false);
    navigate('/asP');
  };

  const handleAutoAssign = () => {
    if (leftCount === 0) {
      setErrorMsg('No TAs left to assign.');
      return;
    }
    const available = filtered.length;
    if (available === 0) {
      setErrorMsg('No TAs available to assign.');
      return;
    }
    const numToAssign = Math.min(leftCount, available);
    const toAdd = filtered.slice(0, numToAssign);
    const newAssigned = [...assigned, ...toAdd];
    const newPotential = potential.filter(t => !toAdd.some(a => a.id === t.id));

    setAssigned(newAssigned);
    setPotential(newPotential);
    examData.assignedTAs = newAssigned;
    examData.potentialTAs = newPotential;
    examData.tasLeft = needed - newAssigned.length;

    // Use ErrPopUp for both success & partial-info
    if (numToAssign < leftCount) {
      setAutoMsg(`Assigned ${numToAssign}, but ${needed - newAssigned.length} still left.`);
    } else {
      setAutoMsg(`Successfully assigned all ${numToAssign} TAs.`);
    }
    setShowAutoErr(true);
  };

  return (
    <div className={styles.pageWrapper}>
      <DepOfNavBar />

      <div className={styles.header}>
        <BackBut to="/asP" />
        <h1 className={styles.title}>
          {examData.courseName} – {examData.examType}
        </h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </div>

      <div className={styles.filters}>
        <label>
          <input
            type="checkbox"
            checked={restrictLevel}
            onChange={e => setRestrictLevel(e.target.checked)}
          /> Level ≥ {examData.level}
        </label>
        <label>
          <input
            type="checkbox"
            checked={excludeAdjacent}
            onChange={e => setExcludeAdjacent(e.target.checked)}
          /> No Adjacent Proctoring
        </label>
        <label>
          <input
            type="checkbox"
            checked={enforceMax}
            onChange={e => setEnforceMax(e.target.checked)}
          /> Max workload
        </label>
        {enforceMax && (
          <input
            type="number"
            min={0}
            value={maxWorkload}
            className={styles.maxInput}
            onChange={e => setMaxWorkload(Number(e.target.value))}
          />
        )}
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
            <th>Name</th><th>Level</th><th>Workload</th><th>Adjacent Proctoring?</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(ta => (
            <tr
              key={ta.id}
              onClick={() => toggleSelect(ta)}
              className={assigned.some(a => a.id === ta.id) ? styles.selectedRow : ''}
            >
              <td>{ta.name}</td>
              <td>{ta.level}</td>
              <td>{ta.workload}</td>
              <td>{ta.hasAdjacentExam ? 'Yes' : 'No'}</td>
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
        <button className={styles.autoButton} onClick={handleAutoAssign}>
          Run Auto Assign
        </button>
        <button className={styles.confirmButton} onClick={() => setShowConfirm(true)}>
          Confirm Assignment
        </button>
      </div>

      {errorMsg && <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />}

      {showConfirm && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={handleConfirm}
          onCancel={() => setShowConfirm(false)}
        />
      )}

      {showAutoErr && autoMsg && (
        <ErrPopUp message={autoMsg} onConfirm={() => setShowAutoErr(false)} />
      )}
    </div>
  );
};

export default AssignTA;
