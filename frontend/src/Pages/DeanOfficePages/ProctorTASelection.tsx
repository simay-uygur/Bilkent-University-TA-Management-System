// src/pages/ProctorLeftTA/ProctorLeftTA.tsx
import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './ProctorTASelection.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
}

const sampleInFaculty: TA[] = [
  { id: 'a1', name: 'Alice Johnson', level: 'MS', workload: 2 },
  { id: 'a2', name: 'Bob Lee',      level: 'BS', workload: 1 },
  { id: 'a3', name: 'Carol Smith',  level: 'PhD', workload: 3 },
];

const sampleOtherFaculty: TA[] = [
  { id: 'b1', name: 'Dan Brown',     level: 'BS', workload: 2 },
  { id: 'b2', name: 'Eve Davis',     level: 'MS', workload: 1 },
  { id: 'b3', name: 'Franklin Clark',level: 'PhD', workload: 4 },
];

const ProctorTASelection: React.FC = () => {
  const navigate = useNavigate();

  const [potentialIn, setPotentialIn]   = useState<TA[]>(sampleInFaculty);
  const [potentialOut, setPotentialOut] = useState<TA[]>(sampleOtherFaculty);
  const [assignedIn, setAssignedIn]     = useState<TA[]>([]);
  const [assignedOut, setAssignedOut]   = useState<TA[]>([]);
  const [searchIn, setSearchIn]         = useState('');
  const [searchOut, setSearchOut]       = useState('');
  const [finishMsg, setFinishMsg]       = useState<string | null>(null);

  const needed = 4;
  const leftCount = Math.max(0, needed - assignedIn.length - assignedOut.length);

  const filteredIn = useMemo(
    () => potentialIn.filter(ta => ta.name.toLowerCase().includes(searchIn.toLowerCase())),
    [potentialIn, searchIn]
  );
  const filteredOut = useMemo(
    () => potentialOut.filter(ta => ta.name.toLowerCase().includes(searchOut.toLowerCase())),
    [potentialOut, searchOut]
  );

  const toggleIn = (ta: TA) => {
    if (assignedIn.some(a => a.id === ta.id)) {
      setAssignedIn(prev => prev.filter(a => a.id !== ta.id));
      setPotentialIn(prev => [...prev, ta]);
    } else if (assignedIn.length + assignedOut.length < needed) {
      setAssignedIn(prev => [...prev, ta]);
      setPotentialIn(prev => prev.filter(a => a.id !== ta.id));
    }
  };

  const toggleOut = (ta: TA) => {
    if (assignedOut.some(a => a.id === ta.id)) {
      setAssignedOut(prev => prev.filter(a => a.id !== ta.id));
      setPotentialOut(prev => [...prev, ta]);
    } else if (assignedIn.length + assignedOut.length < needed) {
      setAssignedOut(prev => [...prev, ta]);
      setPotentialOut(prev => prev.filter(a => a.id !== ta.id));
    }
  };

  const handleFinish = () => {
    if (leftCount > 0) {
      setFinishMsg("You didn't fill the TA need. Are you sure?");
    } else {
      setFinishMsg('Are you sure you want to finish the assignment?');
    }
  };
  const confirmFinish = () => {
    setFinishMsg(null);
    navigate('/deans-office/proctor');
  };

  return (
    <div className={styles.pageWrapper}>

      <div className={styles.headerRow}>
        <BackBut to="/deans-office/proctor" />
        <h1 className={styles.title}>Course of Exams</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </div>

      <div className={styles.bodyWrapper}>
        <div className={styles.splitContainer}>
          <div className={styles.column}>
            <h2>Proctor TA In Faculty</h2>
            <input
              type="text"
              placeholder="Search…"
              value={searchIn}
              onChange={e => setSearchIn(e.target.value)}
              className={styles.searchBar}
            />
            <table className={styles.table}>
              <thead>
                <tr><th>Name</th><th>Level</th><th>Workload</th></tr>
              </thead>
              <tbody>
                {filteredIn.map(ta => (
                  <tr key={ta.id} onClick={() => toggleIn(ta)} className={styles.row}>
                    <td>{ta.name}</td>
                    <td>{ta.level}</td>
                    <td>{ta.workload}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className={styles.column}>
            <h2>Proctor TA Other Faculty</h2>
            <input
              type="text"
              placeholder="Search…"
              value={searchOut}
              onChange={e => setSearchOut(e.target.value)}
              className={styles.searchBar}
            />
            <table className={styles.table}>
              <thead>
                <tr><th>Name</th><th>Level</th><th>Workload</th></tr>
              </thead>
              <tbody>
                {filteredOut.map(ta => (
                  <tr key={ta.id} onClick={() => toggleOut(ta)} className={styles.row}>
                    <td>{ta.name}</td>
                    <td>{ta.level}</td>
                    <td>{ta.workload}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className={styles.assignedContainer}>
          <div className={styles.assignedSection}>
            <h3>Assigned In Faculty</h3>
            <ul>
              {assignedIn.map(ta => (
                <li key={ta.id} onClick={() => toggleIn(ta)}>
                  {ta.name}
                </li>
              ))}
            </ul>
          </div>
          <div className={styles.assignedSection}>
            <h3>Assigned Other Faculty</h3>
            <ul>
              {assignedOut.map(ta => (
                <li key={ta.id} onClick={() => toggleOut(ta)}>
                  {ta.name}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>

      <div className={styles.finishContainer}>
        <button className={styles.finishBtn} onClick={handleFinish}>
          Finish Assignment
        </button>
      </div>

      {finishMsg && (
        <ConPop
          message={finishMsg}
          onConfirm={confirmFinish}
          onCancel={() => setFinishMsg(null)}
        />
      )}
    </div>
  );
};

export default ProctorTASelection;
