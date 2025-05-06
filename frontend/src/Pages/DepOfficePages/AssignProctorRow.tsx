// src/components/AssignProctorRow.tsx
import React from 'react';
import { Check } from 'lucide-react';
import styles from './AssignProctorRow.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  wantedState: 'Prefered' | 'Unprefered' | 'None';
}

export interface Exam {
  id: string;
  courseName: string;
  courseId: string;
  level: 'BS' | 'MS' | 'PhD';
  examType: 'Midterm' | 'Final';
  date: string;
  startTime: string;
  endTime: string;
  needed: number;
  tasLeft: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
}

interface AssignProctorRowProps {
  exam: Exam;
  onAuto: (id: string) => void;
  onFinish: (id: string) => void;
  onDemand: (id: string) => void;    // new prop
}

const AssignProctorRow: React.FC<AssignProctorRowProps> = ({
  exam,
  onAuto,
  onFinish,
  onDemand,             // destructure it
}) => {
  const completed = exam.tasLeft === 0;

  return (
    <tr
      className={`
        ${styles.row}
        ${completed ? styles.completed : styles.incomplete}
      `}
    >
      <td>
        {completed && <Check size={16} className={styles.tick} />} 
        {exam.courseName}
      </td>
      <td>{exam.courseId}</td>
      <td>{exam.level}</td>
      <td>{exam.examType}</td>
      <td>{exam.date}</td>
      <td>{exam.startTime}</td>
      <td>{exam.endTime}</td>
      <td>{exam.needed}</td>
      <td>{exam.tasLeft}</td>
      <td className={styles.actions}>
        <button
          className={`${styles.btn} ${styles.autoBtn}`}
          onClick={() => onAuto(exam.id)}
        >
          Assign TAs
        </button>
        <button
          className={`${styles.btn} ${styles.demandBtn}`}    // new button style
          onClick={() => onDemand(exam.id)}
        >
          Demand TA
        </button>
        <button
          className={`${styles.btn} ${styles.manualBtn}`}
          onClick={() => onFinish(exam.id)}
        >
          Finish Assignment
        </button>
      </td>
    </tr>
  );
};

export default AssignProctorRow;
