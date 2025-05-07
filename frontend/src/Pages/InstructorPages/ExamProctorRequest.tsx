// src/pages/ExamProctor/ExamProctorReq.tsx
import React, { useState, FocusEvent, useEffect } from 'react';
import GreenBut from '../../components/Buttons/GreenBut';
import styles from './ExamProctorRequest.module.css';

export interface Exam {
  id: string;
  name: string;
  type: string;
  studentCount: number;
}

interface ExamProctorReqProps {
  exams: Exam[];
  onSubmit: (requests: { examId: string; neededTAs: number }[]) => void;
  /** ID of the exam whose input should be reset back to zero */
  resetExamId?: string;
  /** callback once you've reset that exam input */
  onResetDone?: () => void;
}

const ExamProctorRequest: React.FC<ExamProctorReqProps> = ({
  exams,
  onSubmit,
  resetExamId,
  onResetDone
}) => {
  // map examId → needed count
  const [neededMap, setNeededMap] = useState(
    exams.reduce((m, e) => {
      m[e.id] = 0;
      return m;
    }, {} as Record<string, number>)
  );

  // when parent tells us to reset a specific exam
  useEffect(() => {
    if (resetExamId && resetExamId in neededMap) {
      setNeededMap(prev => ({ ...prev, [resetExamId]: 0 }));
      onResetDone?.();
    }
  }, [resetExamId]);

  const handleFocus = (e: FocusEvent<HTMLInputElement>) => {
    e.target.select();
  };

  const handleChange = (examId: string, v: number) => {
    setNeededMap(prev => ({ ...prev, [examId]: v }));
  };

  const submitOne = (examId: string) => {
    onSubmit([{ examId, neededTAs: neededMap[examId] }]);
  };

  return (
    <form className={styles.container} onSubmit={e => e.preventDefault()}>
      {exams.map(ex => (
        <div key={ex.id} className={styles.card}>
          <div className={styles.cardHeader}>
            {ex.name} — {ex.type}
          </div>
          <label className={styles.label}>
            TA Needed:
            <input
              type="number"
              min={0}
              value={neededMap[ex.id]}
              onFocus={handleFocus}
              onChange={e => handleChange(ex.id, +e.target.value)}
              className={styles.inputNumber}
            />
          </label>
          <div className={styles.cardSubmitWrapper}>
            <GreenBut
              text="Submit This Exam"
              onClick={() => submitOne(ex.id)}
            />
          </div>
        </div>
      ))}
    </form>
  );
};

export default ExamProctorRequest;
