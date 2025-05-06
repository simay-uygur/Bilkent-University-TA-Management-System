// MakeLeaveReq.tsx
import React, { useState } from 'react';
import TANavBar from '../../components/NavBars/TANavBar';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './MakeLeaveReq.module.css';
import { useNavigate } from 'react-router-dom';

const MakeLeaveReq: React.FC = () => {
  const [startDate, setStartDate] = useState<string>('2025-01-01');
  const [endDate, setEndDate] = useState<string>('2025-01-01');
  const [startTime, setStartTime] = useState<string>('15:40');
  const [endTime, setEndTime] = useState<string>('16:30');
  const [message, setMessage] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const navigate = useNavigate();

  // control popups
  const [showError, setShowError] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.length) setFile(e.target.files[0]);
  };

  // when user clicks the button, we open confirmation dialog
  const handleSubmitClick = () => {
    setShowConfirm(true);
  };

  // after user confirms in ConPop
  const handleConfirmSubmit = () => {
    setShowConfirm(false);

    const start = new Date(`${startDate}T${startTime}`);
    const end = new Date(`${endDate}T${endTime}`);

    if (end < start) {
      setShowError(true);
      return;
    }

    // TODO: replace with real API call
    
    navigate('/ta');
  };

  return (
    <div className={styles.pageWrapper}>
     

      <main className={styles.content}>
        <div className={styles.leaveRequestForm}>

          <div className={styles.calendarsWrapper}>
            <div className={styles.calendar}>
              <input
                type="date"
                value={startDate}
                onChange={e => setStartDate(e.target.value)}
              />
            </div>
            <div className={styles.calendar}>
              <input
                type="date"
                value={endDate}
                onChange={e => setEndDate(e.target.value)}
              />
            </div>
          </div>

          <div className={styles.timeWrapper}>
            <div className={styles.timePicker}>
              <label>Start Time</label>
              <input
                type="time"
                value={startTime}
                onChange={e => setStartTime(e.target.value)}
              />
            </div>
            <div className={styles.timePicker}>
              <label>End Time</label>
              <input
                type="time"
                value={endTime}
                onChange={e => setEndTime(e.target.value)}
              />
            </div>
          </div>

          <textarea
            placeholder="Enter message here..."
            className={styles.messageInput}
            value={message}
            onChange={e => setMessage(e.target.value)}
          />

          <div className={styles.fileUploadSection}>
            <input type="file" onChange={handleFileChange} />
          </div>

          <button
            className={styles.submitButton}
            onClick={handleSubmitClick}
          >
            Make Leave Request
          </button>
        </div>
      </main>

      {showConfirm && (
        <ConPop
          message="Are you sure you want to submit this leave request?"
          onConfirm={handleConfirmSubmit}
          onCancel={() => setShowConfirm(false)}
        />
      )}

      {showError && (
        <ErrPopUp
          message="The end date cannot be before the start date. Please check your dates."
          onConfirm={() => setShowError(false)}
        />
      )}
    </div>
  );
};

export default MakeLeaveReq;
