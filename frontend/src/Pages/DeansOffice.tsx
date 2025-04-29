import React, { useState } from 'react';
import styles from './DeansOffice.module.css';
import NavBarDeans from '../components/NavBarDeans.tsx';

interface TA {
  id: number;
  name: string;
  department: string;
}

const allTAs: TA[] = [
  { id: 1, name: 'John Doe', department: 'Computer Science' },
  { id: 2, name: 'Jane Smith', department: 'Computer Science' },
  { id: 3, name: 'Alan Turing', department: 'Mathematics' },
  { id: 4, name: 'Marie Curie', department: 'Physics' },
];

const DeansOffice: React.FC = () => {
  const [assignedTAs, setAssignedTAs] = useState<TA[]>([]);
  const [autoAssignedTAs, setAutoAssignedTAs] = useState<TA[]>([]);

  const handleAssignProctorManually = (ta: TA) => {
    setAssignedTAs((prevAssignedTAs) => [...prevAssignedTAs, ta]);
  };

  const handleAssignProctorAutomatically = () => {
    // Example: Automatically assign a proctor from all departments
    const taToAutoAssign = allTAs[Math.floor(Math.random() * allTAs.length)];
    setAutoAssignedTAs((prevAutoAssignedTAs) => [...prevAutoAssignedTAs, taToAutoAssign]);
  };

  return (
    <div className={styles.pageWrapper}>
        <div>
      
      <div>Dean's Office Content</div>
    </div>
      <h1>Dean’s Office Proctoring Assignment</h1>
      
      <div className={styles.taList}>
        {allTAs.map((ta) => (
          <div key={ta.id} className={styles.taCard}>
            <div className={styles.taInfo}>
              <span>{ta.name}</span>
              <span>{ta.department}</span>
            </div>
            <button
              className={styles.assignButton}
              onClick={() => handleAssignProctorManually(ta)}
            >
              Assign Proctor (Manual)
            </button>
          </div>
        ))}
      </div>

      <button
        className={styles.autoAssignButton}
        onClick={handleAssignProctorAutomatically}
      >
        Auto Assign Proctor
      </button>

      <div className={styles.assignedTAs}>
        <h2>Manually Assigned Proctors</h2>
        {assignedTAs.length === 0 ? (
          <p>No proctors assigned yet.</p>
        ) : (
          <ul>
            {assignedTAs.map((ta) => (
              <li key={ta.id}>{ta.name}</li>
            ))}
          </ul>
        )}
      </div>

      <div className={styles.autoAssignedTAs}>
        <h2>Automatically Assigned Proctors</h2>
        {autoAssignedTAs.length === 0 ? (
          <p>No auto proctors assigned yet.</p>
        ) : (
          <ul>
            {autoAssignedTAs.map((ta) => (
              <li key={ta.id}>{ta.name}</li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default DeansOffice;
