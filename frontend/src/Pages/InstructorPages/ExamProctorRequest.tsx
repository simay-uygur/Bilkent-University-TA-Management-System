/*import React, { useState, useEffect, FocusEvent } from 'react';
import styles from './ExamProctorRequest.module.css';
import { Exam } from './ExamProctorPage'; // Import the Exam interface from the parent

interface ExamProctorReqProps {
  exams: Exam[];
  onSubmitRequest: (examId: number, requiredTas: number) => void;
  resetExamId: number | null;
  onResetDone: () => void;
  submitting: boolean;
}

const ExamProctorRequest: React.FC<ExamProctorReqProps> = ({
  exams,
  onSubmitRequest,
  resetExamId,
  onResetDone,
  submitting
}) => {
  // Map to store the number of TAs needed for each exam
  const [neededMap, setNeededMap] = useState<Record<number, number>>({});
  const [descriptionMap, setDescriptionMap] = useState<Record<number, string>>({});
  
  
  // Initialize the neededMap when exams change
  useEffect(() => {
    const initialNeededMap: Record<number, number> = {};
    const initialDescriptionMap: Record<number, string> = {};
    exams.forEach(exam => {
      initialNeededMap[exam.examId] = 0;
      initialDescriptionMap[exam.examId] = '';
    
    });
    setNeededMap(initialNeededMap);
    setDescriptionMap(initialDescriptionMap);
  }, [exams]);
  
  // Reset the needed TAs for a specific exam when requested
  useEffect(() => {
    if (resetExamId !== null) {
      setNeededMap(prev => ({ ...prev, [resetExamId]: 0 }));
      setDescriptionMap(prev => ({ ...prev, [resetExamId]: '' }));
      onResetDone();
    }
  }, [resetExamId, onResetDone]);
  
  const handleFocus = (e: FocusEvent<HTMLInputElement>) => {
    e.target.select();
  };
  
  const handleChange = (examId: number, v: number) => {
    setNeededMap(prev => ({ ...prev, [examId]: v }));
  };
  const handleDescriptionChange = (examId: number, value: string) => {
    setDescriptionMap(prev => ({ ...prev, [examId]: value }));
  };
  
  // Format date for display
  const formatDate = (date: any) => {
    return `${date.day}/${date.month}/${date.year}`;
  };
  
  // Format time for display
  const formatTime = (date: any) => {
    return `${String(date.hour).padStart(2, '0')}:${String(date.minute).padStart(2, '0')}`;
  };
  
  if (exams.length === 0) {
    return (
      <div className={styles.noExams}>
        <p>No exams available for proctor requests.</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      {exams.map(exam => (
        <div key={exam.examId} className={styles.examCard}>
          <div className={styles.cardHeader}>
            <h3>{exam.type}</h3>
            <span className={styles.courseCode}>{exam.courseCode}</span>
          </div>
          
          <div className={styles.examDetails}>
            <div className={styles.examInfo}>
              <span className={styles.infoLabel}>Date:</span>
              <span>{formatDate(exam.duration.start)}</span>
            </div>
            <div className={styles.examInfo}>
              <span className={styles.infoLabel}>Time:</span>
              <span>
                {formatTime(exam.duration.start)} - {formatTime(exam.duration.finish)}
              </span>
            </div>
            <div className={styles.examInfo}>
              <span className={styles.infoLabel}>Rooms:</span>
              <span>{exam.examRooms.join(', ')}</span>
            </div>
          </div>
          
          <div className={styles.inputGroup}>
             <div className={styles.inputRow}>
              <label htmlFor={`ta-${exam.examId}`} className={styles.label}>
                TAs Needed:
              </label>
              <input
                id={`ta-${exam.examId}`}
                type="number"
                min="0"
                value={neededMap[exam.examId] || 0}
                onFocus={handleFocus}
                onChange={(e) => handleChange(exam.examId, Math.max(0, parseInt(e.target.value, 10) || 0))}
                className={styles.inputNumber}
                disabled={submitting}
              />
            </div>
            <div className={styles.textareaContainer}>
              <label htmlFor={`desc-${exam.examId}`} className={styles.label}>
                Additional Notes:
              </label>
              <textarea
                id={`desc-${exam.examId}`}
                value={descriptionMap[exam.examId] || ''}
                onChange={(e) => handleDescriptionChange(exam.examId, e.target.value)}
                className={styles.descriptionInput}
                placeholder="Any specific requirements or information..."
                disabled={submitting}
                rows={3}
              />
            </div>
            <button
              className={styles.submitButton}
              onClick={() => onSubmitRequest(exam.examId, neededMap[exam.examId] || 0)}
              disabled={!neededMap[exam.examId] || neededMap[exam.examId] <= 0 || submitting}
            >
              {submitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

export default ExamProctorRequest;*/
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
  /// ID of the exam whose input should be reset back to zero 
  resetExamId?: string;
  /// callback once you've reset that exam input 
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
