// src/pages/AssignProctor/AssignProctor.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DepOfNavBar from '../../components/NavBars/DepOfNavBar';
import AssignProctorRow, { Exam } from './AssignProctorRow';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import { sampleExams } from './data';
import styles from './AssignProctor.module.css';

const AssignProctor: React.FC = () => {
  const navigate = useNavigate();
  const [exams, setExams] = useState<Exam[]>(sampleExams);

  // finish-assignment popups
  const [confirmId, setConfirmId] = useState<string | null>(null);
  const [confirmMsg, setConfirmMsg] = useState<string | null>(null);

  // demand-TA popups
  const [demandId, setDemandId] = useState<string | null>(null);
  const [demandError, setDemandError] = useState<string | null>(null);
  const [demandConfirmMsg, setDemandConfirmMsg] = useState<string | null>(null);

  const handleAuto = (id: string) => {
    navigate(`assign/${id}`);
  };

  const handleFinish = (id: string) => {
    const exam = exams.find(e => e.id === id);
    if (!exam) return;
    setConfirmMsg(
      exam.tasLeft > 0
        ? "You didn't fill needed TAs. Are you sure?"
        : 'Mark this assignment as finished?'
    );
    setConfirmId(id);
  };

  const handleConfirmFinish = () => {
    if (confirmId) {
      setExams(prev => prev.filter(e => e.id !== confirmId));
    }
    setConfirmMsg(null);
    setConfirmId(null);
  };

  const handleDemand = (id: string) => {
    const exam = exams.find(e => e.id === id);
    if (!exam) return;

    if (exam.tasLeft === 0) {
      setDemandError('Cannot request more TAs: none left.');
      return;
    }

    // Immediately show confirmation popup
    setDemandConfirmMsg(
      "Your assigning authorization will end when you request TAs from the dean’s office. Are you sure?"
    );
    setDemandId(id);
    setDemandError(null);
  };

  const handleConfirmDemand = () => {
    if (demandId) {
      // placeholder for actual demand logic
      setExams(prev => prev.filter(e => e.id !== demandId));
    }
    setDemandConfirmMsg(null);
    setDemandId(null);
    setDemandError(null);
  };

  return (
    <div className={styles.pageWrapper}>
      

      <div className={styles.container}>
        <h1 className={styles.title}>Exams of Courses</h1>
        <table className={styles.table}>
          <thead className={styles.headings}>
            <tr>
              <th>Course Name</th>
              <th>Course ID</th>
              <th>Level</th>
              <th>Exam Type</th>
              <th>Date</th>
              <th>Start</th>
              <th>End</th>
              <th>Needed</th>
              <th>TAs Left</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {exams.map(exam => (
              <AssignProctorRow
                key={exam.id}
                exam={exam}
                onAuto={handleAuto}
                onFinish={handleFinish}
                onDemand={handleDemand}
              />
            ))}
          </tbody>
        </table>
      </div>

      {/* finish-assignment popup */}
      {confirmMsg && (
        <ConPop
          message={confirmMsg}
          onConfirm={handleConfirmFinish}
          onCancel={() => setConfirmMsg(null)}
        />
      )}

      {/* demand-TA immediate error */}
      {demandError && (
        <ErrPopUp
          message={demandError}
          onConfirm={() => setDemandError(null)}
        />
      )}

      {/* demand-TA confirmation popup */}
      {demandConfirmMsg && (
        <ConPop
          message={demandConfirmMsg}
          onConfirm={handleConfirmDemand}
          onCancel={() => setDemandConfirmMsg(null)}
        />
      )}
    </div>
  );
};

export default AssignProctor;
