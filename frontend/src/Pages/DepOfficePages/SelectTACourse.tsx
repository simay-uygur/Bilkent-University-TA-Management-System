// src/pages/AssignTA/SelectTACourse.tsx
import React, { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import DepOfNavBar from '../../components/NavBars/DepOfNavBar';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectTACourse.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

const sampleCourses: Course[] = [
  {
    id: 'c2',
    courseName: 'CS202 – Data Structures',
    needed: 3,
    assignedTAs: [],
    potentialTAs: [
      { id: 't4', name: 'David Kim', level: 'MS', workload: 4, hasAdjacentExam: false, preference: 'preferred' },
      { id: 't5', name: 'Eva Martinez', level: 'BS', workload: 2, hasAdjacentExam: false, preference: 'unpreferred' },
      { id: 't6', name: 'Frank Zhou', level: 'PhD', workload: 6, hasAdjacentExam: true, preference: 'normal' },
    ],
  },
];

const SelectTACourse: React.FC = () => {
  const navigate = useNavigate();
  const { courseId } = useParams<{ courseId: string }>();
  const courseData = sampleCourses.find(c => c.id === courseId) || sampleCourses[0];

  const needed = courseData.needed;
  const [assigned, setAssigned] = useState<TA[]>([...courseData.assignedTAs]);
  const [potential, setPotential] = useState<TA[]>([...courseData.potentialTAs]);

  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);

  const leftCount = Math.max(0, needed - assigned.length);
  courseData.tasLeft = leftCount;

  const assignTA = (ta: TA) => {
    const newAssigned = [...assigned, ta];
    setAssigned(newAssigned);
    setPotential(p => p.filter(a => a.id !== ta.id));
  };

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };

  const handleUnprefConfirm = () => {
    if (selectedUnpref) assignTA(selectedUnpref);
    setShowUnprefConfirm(false);
  };

  const sortedPotential = useMemo(
    () => [...potential].sort((a, b) => a.workload - b.workload),
    [potential]
  );

  const handleConfirm = () => setConfirmAssign(true);
  const doConfirm = () => { setConfirmAssign(false); navigate('/asgnTAC'); };

  return (
    <div className={styles.pageWrapper}>
      <DepOfNavBar />

      <header className={styles.header}>
        <BackBut to="/asgnTAC" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
          </tr>
        </thead>
        <tbody>
          {sortedPotential.map(ta => {
            const isAssigned = assigned.some(a => a.id === ta.id);
            const rowClass = isAssigned
              ? styles.assignedRow
              : ta.preference === 'preferred'
              ? styles.preferredRow
              : ta.preference === 'unpreferred'
              ? styles.unpreferredRow
              : '';
            return (
              <tr
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={`${styles.rowBase} ${rowClass}`}
              >
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'
                ? styles.preferredItem
                : ta.preference === 'unpreferred'
                ? styles.unpreferredItem
                : styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button className={styles.confirmButton} onClick={handleConfirm}>
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={doConfirm}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectTACourse;
