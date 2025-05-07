import React, { useState } from 'react';
import { Check, X, Info, List } from 'lucide-react';
import styles from './LeaveReqMes.module.css';

interface WorkloadItem {
  date: Date;
  hour: number;
  minute: number;
  description: string;
}

interface LeaveReqMesProps {
  excuse: string;  
  taName: string;
  startDate: Date;
  endDate: Date;
  onApprove?: () => void;
  onReject?: () => void;
  fileUrl?: string;
  workloads?: {
    proctorings: WorkloadItem[];
    labs: WorkloadItem[];
    recitations: WorkloadItem[];
  };
}

const pad = (n: number) => n.toString().padStart(2, '0');
const formatDate = (date: Date) => {
  const day = pad(date.getDate());
  const month = pad(date.getMonth() + 1);
  const year = date.getFullYear();
  const hour = pad(date.getHours());
  const minute = pad(date.getMinutes());
  return `${day}.${month}.${year} ${hour}:${minute}`;
};

const LeaveReqMes: React.FC<LeaveReqMesProps> = ({
  excuse,
  taName,
  startDate,
  endDate,
  onApprove,
  onReject,
  fileUrl,
  workloads,
}) => {
  const [visible, setVisible] = useState(true);
  const [showInfo, setShowInfo] = useState(false);
  const [showWorkloads, setShowWorkloads] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

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

  const handleReject = () => {
    setVisible(false);
    onReject?.();
  };

  if (!visible) return null;

  return (
    <>
      <div className={styles.container}>
        <p className={styles.message}>
          {taName} wants to leave from{' '}
          <span className={styles.dateSegment}>{formatDate(startDate)}</span> to{' '}
          <span className={styles.dateSegment}>{formatDate(endDate)}</span>.
        </p>
        <div className={styles.buttons}>
          <button className={styles.approve} onClick={handleApproveClick} aria-label="Approve">
            <Check />
          </button>
          <button className={styles.reject} onClick={handleReject} aria-label="Reject">
            <X />
          </button>
          <button className={styles.info} onClick={() => setShowInfo(true)} aria-label="Info">
            <Info />
          </button>
          <button className={styles.workloadBtn} onClick={() => setShowWorkloads(true)} aria-label="Workloads">
            <List />
          </button>
        </div>
      </div>

      {/* Approval Confirmation Popup */}
      {showConfirm && (
        <div className={styles.confirmOverlay}>
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

      {/* Info Popup */}
      {showInfo && (
        <div className={styles.popupOverlay}>
          <div className={styles.popup}>
            <p className={styles.popupMessage}>{excuse}</p>
            {fileUrl && (
              <a href={fileUrl} download className={styles.downloadLink}>
                Download Attachment
              </a>
            )}
            <button className={styles.popupCancel} onClick={() => setShowInfo(false)}>
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* Workloads Popup */}
      {showWorkloads && (
        <div className={styles.workloadOverlay}>
          <div className={styles.workloadPopup}>
            <h3 className={styles.popupTitle}>All Workloads for {taName}</h3>
            <div className={styles.workloadList}>
              <section className={styles.workloadSection}>
                <h4>Proctorings</h4>
                {workloads?.proctorings.map((item, idx) => (
                  <p key={idx}>{`${formatDate(item.date)} ${pad(item.hour)}:${pad(item.minute)} - ${item.description}`}</p>
                ))}
              </section>
              <section className={styles.workloadSection}>
                <h4>Labs</h4>
                {workloads?.labs.map((item, idx) => (
                  <p key={idx}>{`${formatDate(item.date)} ${pad(item.hour)}:${pad(item.minute)} - ${item.description}`}</p>
                ))}
              </section>
              <section className={styles.workloadSection}>
                <h4>Recitations</h4>
                {workloads?.recitations.map((item, idx) => (
                  <p key={idx}>{`${formatDate(item.date)} ${pad(item.hour)}:${pad(item.minute)} - ${item.description}`}</p>
                ))}
              </section>
            </div>
            <button className={styles.closeButton} onClick={() => setShowWorkloads(false)}>
              Close
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default LeaveReqMes;