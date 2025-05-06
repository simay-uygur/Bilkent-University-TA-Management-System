// src/pages/ExamProctor/ExamProctorPage.tsx
import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import InsNavBar from '../../components/NavBars/InsNavBar';
import BackBut from '../../components/Buttons/BackBut';
import ExamProctorReq, { Exam} from './ExamProctorReq';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './ExamProctorPage.module.css';

interface RawRequest {
  examId: string;
  neededTAs: number;
}

interface BackendPayload {
  examId: string;
  name: string;
  type: string;
  studentCount: number;
  neededTAs: number;
}

const ExamProctorPage: React.FC = () => {
  const location = useLocation();
  const courseCode = location.pathname.split('/')[2] || 'Unknown';

  const initialExams: Exam[] = [
    { id: `${courseCode}-mid`,   name: `${courseCode} Midterm`, type: 'Midterm', studentCount: 120 },
    { id: `${courseCode}-final`, name: `${courseCode} Final`,   type: 'Final',   studentCount: 120 },
  ];

  const [exams] = useState<Exam[]>(initialExams);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [confirmData, setConfirmData] = useState<RawRequest | null>(null);
  const [resetExamId, setResetExamId] = useState<string | null>(null);

  const handleSubmit = (requests: RawRequest[]) => {
    const req = requests[0];
    if (req.neededTAs === 0) {
      setErrorMsg("You can't request 0 TAs.");
    } else {
      setConfirmData(req);
    }
  };

  const handleConfirm = () => {
    if (!confirmData) return;
    const { examId, neededTAs } = confirmData;
    const exam = exams.find(e => e.id === examId)!;
    const payload: BackendPayload = {
      examId,
      name: exam.name,
      type: exam.type,
      studentCount: exam.studentCount,
      neededTAs,
    };
    console.log('Sending to backend:', payload);
    // TODO: POST payload to your API

    // close popup & reset only that exam’s input
    setConfirmData(null);
    setResetExamId(examId);
  };

  return (
    <div className={styles.container}>
      

      <div className={styles.headerRow}>
        <BackBut to="/ins" />
        <h1 className={styles.title}>Exam Proctoring for {courseCode}</h1>
      </div>

      <div className={styles.mainContainer}>
        <ExamProctorReq
          exams={exams}
          onSubmit={handleSubmit}
          resetExamId={resetExamId || undefined}
          onResetDone={() => setResetExamId(null)}
        />
      </div>

      {errorMsg && (
        <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />
      )}

      {confirmData && (
        <ConPop
          message="Are you sure you want to submit this request?"
          onConfirm={handleConfirm}
          onCancel={() => setConfirmData(null)}
        />
      )}
    </div>
  );
};

export default ExamProctorPage;
