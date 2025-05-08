// src/pages/AssignProctor/ProctorCourseSelectionIn.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import styles from './ProctorCourseSelectionIn.module.css';
import BackBut from '../../components/Buttons/BackBut';

interface Exam {
  id: string;
  courseName: string;
  courseId: string;
  level: string;
  examType: string;
  date: string;
  startTime: string;
  endTime: string;
  needed: number;
  tasLeft: number;
}

const initialExams: Exam[] = [
  {
    id: 'e1',
    courseName: 'Intro to CS',
    courseId: 'CS-101',
    level: 'BS',
    examType: 'Midterm',
    date: '2025-05-20',
    startTime: '10:00',
    endTime: '12:00',
    needed: 3,
    tasLeft: 2,
  },
  {
    id: 'e2',
    courseName: 'Calculus II',
    courseId: 'MATH-201',
    level: 'MS',
    examType: 'Final',
    date: '2025-05-22',
    startTime: '14:00',
    endTime: '16:00',
    needed: 2,
    tasLeft: 1,
  },
  {
    id: 'e3',
    courseName: 'Physics III',
    courseId: 'PHY-301',
    level: 'PhD',
    examType: 'Midterm',
    date: '2025-05-25',
    startTime: '09:00',
    endTime: '11:00',
    needed: 1,
    tasLeft: 0,
  },
];

const ProctorCourseSelectionIn: React.FC = () => {
  const navigate = useNavigate();
  const [exams, setExams] = useState<Exam[]>(initialExams);

  // finish-assignment popups
  const [confirmId, setConfirmId] = useState<string | null>(null);
  const [confirmMsg, setConfirmMsg] = useState<string | null>(null);
  
  // demand-TA popups
  const [demandId, setDemandId] = useState<string | null>(null);
  const [demandError, setDemandError] = useState<string | null>(null);
  const [demandConfirmMsg, setDemandConfirmMsg] = useState<string | null>(null);

  const handleAssign = (id: string) => {
    navigate(`/deans-office/proctor-in/${id}`);
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
    setDemandError(null);
    setDemandConfirmMsg(
      "Requesting from the dean’s office will revoke your current authorizations. Proceed?"
    );
    setDemandId(id);
  };

  const handleConfirmDemand = () => {
    if (demandId) {
      setExams(prev => prev.filter(e => e.id !== demandId));
    }
    setDemandConfirmMsg(null);
    setDemandId(null);
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/deans-office" />
        <h1 className={styles.title}>Exams Proctor Request In Faculty</h1>
      </div>

      <div className={styles.container}>
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
              <tr key={exam.id}>
                <td>{exam.courseName}</td>
                <td>{exam.courseId}</td>
                <td>{exam.level}</td>
                <td>{exam.examType}</td>
                <td>{exam.date}</td>
                <td>{exam.startTime}</td>
                <td>{exam.endTime}</td>
                <td>{exam.needed}</td>
                <td>{exam.tasLeft}</td>
                <td className={styles.actions}>
                  <button onClick={() => handleAssign(exam.id)}>
                    Assign TAs
                  </button>
                  <button onClick={() => handleDemand(exam.id)}>
                    Demand TA
                  </button>
                  <button onClick={() => handleFinish(exam.id)}>
                    Finish Assignment
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {confirmMsg && (
        <ConPop
          message={confirmMsg}
          onConfirm={handleConfirmFinish}
          onCancel={() => setConfirmMsg(null)}
        />
      )}

      {demandError && (
        <ErrPopUp message={demandError} onConfirm={() => setDemandError(null)} />
      )}

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

export default ProctorCourseSelectionIn;
