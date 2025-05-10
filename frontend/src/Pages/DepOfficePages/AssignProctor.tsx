// src/pages/AssignProctor/AssignProctor.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AssignProctorRow, { Exam } from './AssignProctorRow';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import styles from './AssignProctor.module.css';
import BackBut from '../../components/Buttons/BackBut';

const sampleExams: Exam[] = [
  {
    id: 'e1',
    courseName: 'Algorithms',
    courseId: 'CS225',
    level: 'BS',
    examType: 'Midterm',
    date: '2025-05-10',
    startTime: '09:00',
    endTime: '11:00',
    needed: 3,
    tasLeft: 1,
    assignedTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'Prefered' },
      { id: 'ta2', name: 'Ayşe Fatma', level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'None'     },
    ],
    potentialTAs: [
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, wantedState: 'Unprefered' },
      { id: 'ta4', name: 'Jane Doe',    level: 'BS',  workload: 3, hasAdjacentExam: true,  wantedState: 'None'       },
    ],
  },
  {
    id: 'e2',
    courseName: 'Data Structures',
    courseId: 'CS226',
    level: 'MS',
    examType: 'Final',
    date: '2025-06-01',
    startTime: '13:00',
    endTime: '15:00',
    needed: 4,
    tasLeft: 0,
    assignedTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'None'     },
      { id: 'ta2', name: 'Ayşe Fatma', level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'Prefered' },
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, wantedState: 'Unprefered' },
      { id: 'ta4', name: 'John Smith', level: 'BS',  workload: 3, hasAdjacentExam: true,  wantedState: 'None'       },
    ],
    potentialTAs: [
      { id: 'ta5', name: 'Emily Johnson', level: 'PhD', workload: 5, hasAdjacentExam: true, wantedState: 'None' },
    ],
  },
  {
    id: 'e3',
    courseName: 'Machine Learning',
    courseId: 'CS450',
    level: 'PhD',
    examType: 'Midterm',
    date: '2025-05-20',
    startTime: '10:00',
    endTime: '12:00',
    needed: 2,
    tasLeft: 2,
    assignedTAs: [],
    potentialTAs: [
      { id: 'ta1', name: 'Ali Veli',        level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'Prefered' },
      { id: 'ta2', name: 'Ayşe Fatma',     level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'None'     },
    ],
  },
];

const AssignProctor: React.FC = () => {
  const navigate = useNavigate();
  const [exams, setExams] = useState<Exam[]>(sampleExams);

  // finish-assignment popups
  const [confirmId, setConfirmId]     = useState<string|null>(null);
  const [confirmMsg, setConfirmMsg]   = useState<string|null>(null);
  // demand-TA popups
  const [demandId, setDemandId]         = useState<string|null>(null);
  const [demandError, setDemandError]   = useState<string|null>(null);
  const [demandConfirmMsg, setDemandConfirmMsg] = useState<string|null>(null);

  const handleAuto = (id: string) => {
    navigate(`/department-office/assign-proctor/${id}`);
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
    if (confirmId) setExams(prev => prev.filter(e => e.id !== confirmId));
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
    setDemandConfirmMsg(
      "Requesting from the dean’s office will revoke your current authorizations. Proceed?"
    );
    setDemandId(id);
    setDemandError(null);
  };

  const handleConfirmDemand = () => {
    if (demandId) setExams(prev => prev.filter(e => e.id !== demandId));
    setDemandConfirmMsg(null);
    setDemandId(null);
    setDemandError(null);
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>Course of Exams</h1>
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

      {confirmMsg && (
        <ConPop message={confirmMsg} onConfirm={handleConfirmFinish}
          onCancel={() => setConfirmMsg(null)} />
      )}
      {demandError && (
        <ErrPopUp message={demandError} onConfirm={() => setDemandError(null)} />
      )}
      {demandConfirmMsg && (
        <ConPop message={demandConfirmMsg}
          onConfirm={handleConfirmDemand}
          onCancel={() => setDemandConfirmMsg(null)} />
      )}
    </div>
  );
};

export default AssignProctor;
