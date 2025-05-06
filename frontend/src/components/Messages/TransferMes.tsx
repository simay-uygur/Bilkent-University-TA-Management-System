import React, { useState } from 'react';
import { Check, X } from 'lucide-react';
import styles from './TransferMes.module.css';

interface TransferMesProps {
  taName: string;
  course: string;
  courseId: string;
  date: Date;
  hour: number;
  minute: number;
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

const TransferMes: React.FC<TransferMesProps> = ({
  taName,
  course,
  courseId,
  date,
  hour,
  minute,
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
    setShowConfirm(false);
    setVisible(false);
    onApprove?.();
  };

  const handleCancel = () => {
    setShowConfirm(false);
  };

  if (!visible) return null;

  return (
    <>
      <div className={styles.container}>
        <span className={styles.message}>
          {`${taName} demands transfer for proctoring of `}
          <span className={styles.segment}>
            {`${course}-${courseId} on ${formatDateTime(date, hour, minute)}`}
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
              <button className={styles.confirmCancel} onClick={handleCancel}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default TransferMes;
