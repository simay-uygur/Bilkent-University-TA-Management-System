import React from 'react';
import { Check, Trash2 } from 'lucide-react';
import styles from './AssignProctorRow.module.css';

// Import or redefine the TA and Exam interfaces to match AssignProctor.tsx
export interface TA {
  id: string | number;
  name: string;
  level: string;
  workload: number;
  hasAdjacentExam: boolean;
  wantedState: 'Prefered' | 'Unprefered' | 'None';
}

export interface Exam {
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
  assignedTAs: TA[];
  potentialTAs: TA[];
  requestId?: number;
  examId?: number;
  examRooms?: string[];
}

interface AssignProctorRowProps {
  exam: Exam;
  onAuto: (id: string) => void;
  onFinish: (id: any) => void;
  onDemand: (id: string) => void;
  onDelete: (id: string) => void; // New prop for delete functionality
}

const AssignProctorRow: React.FC<AssignProctorRowProps> = ({
  exam,
  onAuto,
  onFinish,
  onDemand,
  onDelete, // Destructure the new prop
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
          className={`${styles.btn} ${styles.assignBtn}`}
          onClick={() => {
            console.log(`Assign button clicked for exam ${exam.id}`);
            onAuto(exam.id);
          }}
        >
          Assign TAs
        </button>
        <button
          className={`${styles.btn} ${styles.demandBtn}`}
          onClick={() => {
            console.log(`Request TAs button clicked for exam ${exam.id}`);
            onDemand(exam.id);
          }}
          disabled={exam.tasLeft === 0}
        >
          Request TAs
        </button>
        <button
          className={`${styles.btn} ${styles.finishBtn}`}
          onClick={() => {
            console.log(`Finish button clicked for exam ${exam.requestId}`);
            onFinish(exam.requestId);
          }}
          disabled={exam.needed > 0 && exam.assignedTAs.length === 0}
        >
          Finish Assignment
        </button>
        <button
          className={`${styles.btn} ${styles.deleteBtn}`}
          onClick={() => {
            console.log(`Delete button clicked for exam ${exam.id}`);
            onDelete(exam.id);
          }}
        >
          <Trash2 size={16} /> Delete
        </button>
      </td>
    </tr>
  );
};

export default AssignProctorRow;
/* import React from 'react';
import { Check } from 'lucide-react';
import styles from './AssignProctorRow.module.css';

// Import or redefine the TA and Exam interfaces to match AssignProctor.tsx
export interface TA {
  id: string | number;
  name: string;
  level: string; // Changed from enum to string to be more flexible
  workload: number;
  hasAdjacentExam: boolean;
  wantedState: 'Prefered' | 'Unprefered' | 'None';
}

export interface Exam {
  id: string;
  courseName: string;
  courseId: string;
  level: string; // Changed from enum to string
  examType: string; // Changed from enum to string
  date: string;
  startTime: string;
  endTime: string;
  needed: number;
  tasLeft: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  requestId?: number; // Added to link back to the original request
  examId?: number;    // Added to fetch available TAs
}

interface AssignProctorRowProps {
  exam: Exam;
  onAuto: (id: string) => void;
  onFinish: (id: string) => void;
  onDemand: (id: string) => void;
}

const AssignProctorRow: React.FC<AssignProctorRowProps> = ({
  exam,
  onAuto,
  onFinish,
  onDemand,
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
          className={`${styles.btn} ${styles.assignBtn}`}
          onClick={() => onAuto(exam.id)}
        >
          Assign TAs
        </button>
        <button
          className={`${styles.btn} ${styles.demandBtn}`}
          onClick={() => onDemand(exam.id)}
          disabled={exam.tasLeft === 0}
        >
          Request TAs
        </button>
        <button
          className={`${styles.btn} ${styles.finishBtn}`}
          onClick={() => onFinish(exam.id)}
          disabled={exam.needed > 0 && exam.assignedTAs.length === 0}
        >
          Finish Assignment
        </button>
      </td>
    </tr>
  );
};

export default AssignProctorRow; */
/* // src/components/AssignProctorRow.tsx
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
          className={`${styles.btn} ${styles.autoBtn}`}    // new button style
          onClick={() => onDemand(exam.id)}
        >
          Demand TA
        </button>
        <button
          className={`${styles.btn} ${styles.autoBtn}`}
          onClick={() => onFinish(exam.id)}
        >
          Finish Assignment
        </button>
      </td>
    </tr>
  );
};

export default AssignProctorRow;
 */