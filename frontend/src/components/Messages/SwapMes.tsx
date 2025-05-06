import React, { useState } from 'react';
import { Check, X } from 'lucide-react';
import styles from './SwapMes.module.css';

interface SwapMesProps {
  taName: string;
  fromCourse: string;
  fromCourseId: string;
  fromDate: Date;
  fromHour: number;
  fromMinute: number;
  toCourse: string;
  toCourseId: string;
  toDate: Date;
  toHour: number;
  toMinute: number;
  onApprove?: () => void;
  onReject?: () => void;
}

const pad = (n: number) => n.toString().padStart(2, '0');
const formatDateTime = (date: Date, hour: number, minute: number) => {
  const d = pad(date.getDate());
  const m = pad(date.getMonth() + 1);
  const y = date.getFullYear();
  return `${d}.${m}.${y} ${pad(hour)}:${pad(minute)}`;
};

const SwapMes: React.FC<SwapMesProps> = ({
  taName,
  fromCourse,
  fromCourseId,
  fromDate,
  fromHour,
  fromMinute,
  toCourse,
  toCourseId,
  toDate,
  toHour,
  toMinute,
  onApprove,
  onReject,
}) => {
  const [visible, setVisible] = useState(true);
  const [showConfirm, setShowConfirm] = useState(false);

  const handleReject = () => {
    setVisible(false);
    onReject?.();
  };
  const handleApproveClick = () => {
    setShowConfirm(true);
  };
  const handleConfirmApprove = () => {
    setVisible(false);
    setShowConfirm(false);
    onApprove?.();
  };
  const handleCancelConfirm = () => {
    setShowConfirm(false);
  };

  if (!visible) return null;

  return (
    <>
      <div className={styles.container}>
        <span className={styles.message}>
          {`${taName} demands swap for proctoring of `}
          <span className={styles.fromSegment}>
            {`${fromCourse}-${fromCourseId} at ${formatDateTime(fromDate, fromHour, fromMinute)}`}
          </span>
          {' with '}
          <span className={styles.toSegment}>
            {`${toCourse}-${toCourseId} at ${formatDateTime(toDate, toHour, toMinute)}`}
          </span>
          {'.'}
        </span>
        <div className={styles.buttons}>
          <button className={styles.approve} onClick={handleApproveClick} aria-label="Approve">
            <Check />
          </button>
          <button className={styles.reject} onClick={handleReject} aria-label="Reject">
            <X />
          </button>
        </div>
      </div>

      {showConfirm && (
        <div className={styles.overlay}>
          <div className={styles.confirmBox}>
            <p className={styles.confirmText}>Are you sure?</p>
            <div className={styles.confirmButtons}>
              <button className={styles.confirmApprove} onClick={handleConfirmApprove}>
                Approve
              </button>
              <button className={styles.confirmCancel} onClick={handleCancelConfirm}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default SwapMes;