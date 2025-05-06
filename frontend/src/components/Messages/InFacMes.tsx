import React, { useState } from 'react';
import styles from './InFacMes.module.css';

interface InFacMesProps {
  departmentName: string;
  demandedTANum: number;
  course: string;
  courseId: string;
  date: Date;
  hour: number;
  minute: number;
  onAssign?: () => void;
}

const pad = (n: number) => n.toString().padStart(2, '0');
const formatDateTime = (date: Date, hour: number, minute: number) => {
  const d = pad(date.getDate());
  const m = pad(date.getMonth() + 1);
  const y = date.getFullYear();
  return `${d}.${m}.${y} ${pad(hour)}:${pad(minute)}`;
};

const InFacMes: React.FC<InFacMesProps> = ({
  departmentName,
  demandedTANum,
  course,
  courseId,
  date,
  hour,
  minute,
  onAssign,
}) => {
  const [visible, setVisible] = useState(true);

  const handleAssign = () => {
    onAssign?.();
  };

  if (!visible) return null;

  return (
    <div className={styles.container}>
      <p className={styles.message}>
        <span className={styles.label}>{departmentName} demands </span>
        <span className={styles.blueBold}>{demandedTANum} TA(s)</span>
        <span className={styles.label}> for the proctoring of </span>
        <span className={styles.greenBold}>{course}-{courseId} at {formatDateTime(date, hour, minute)}</span>
        <span className={styles.label}> from in faculty.</span>
      </p>
      <button className={styles.assignButton} onClick={handleAssign}>
        Assign TA From Own Faculty
      </button>
    </div>
  );
};

export default InFacMes;