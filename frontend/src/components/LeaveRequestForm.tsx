// src/components/LeaveRequestForm.tsx

import React, { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './LeaveRequestForm.module.css';

interface ScheduleItem {
  id: string;
  task: string;
  date: string;       // ISO date
  timeRange: string;  // "HH:MM – HH:MM"
}

// **Mock data** for convenience
const mockItem: ScheduleItem = {
  id: '1',
  task: 'Proctoring – CS101',
  date: '2025-04-20',
  timeRange: '08:00 – 10:00',
};

const LeaveRequestForm: React.FC = () => {
  const navigate = useNavigate();
  const [item]     = useState<ScheduleItem>(mockItem);
  const [startTime, setStartTime] = useState(item.timeRange.split(' – ')[0]);
  const [endTime,   setEndTime]   = useState(item.timeRange.split(' – ')[1]);
  const [excuse,    setExcuse]    = useState('Personal');
  const [message,   setMessage]   = useState<string>('');
  const [attachment, setAttachment] = useState<File | null>(null);

  const handleFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    setAttachment(e.target.files?.[0] ?? null);
  };
  // useEffect(() => {
  //   fetchScheduleItem(scheduleId)
  //     .then(r => {
  //       const it = r.data;
  //       setStartTime(it.timeRange.split(' - ')[0]);
  //       setEndTime(it.timeRange.split(' - ')[1]);
  //     });
  // }, [scheduleId]);
  
  // const onSubmit = (e: FormEvent) => {
  //   e.preventDefault();
  //   submitLeaveRequest({ scheduleId, startTime, endTime, excuse, message }, file)
  //     .then(() => navigate('/dashboard'))
  //     .catch(err => setError(err.message));
  // };
  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    // For now just log and navigate back
    console.log({
      scheduleId: item.id,
      startTime,
      endTime,
      excuse,
      message,
      attachment,
    });
    navigate('/dashboard', { replace: true });
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <button onClick={() => navigate(-1)} className={styles.backBtn}>
          ← Back
        </button>
        <h1 className={styles.title}>Leave Request</h1>
      </header>

      <main className={styles.formCard}>
        <p><strong>Assignment:</strong> {item.task}</p>
        <p><strong>Date:</strong> {new Date(item.date).toLocaleDateString()}</p>

        <form onSubmit={handleSubmit}>
          <div className={styles.field}>
            <label>Start Time</label>
            <input
              type="time"
              value={startTime}
              onChange={e => setStartTime(e.target.value)}
              required
            />
          </div>

          <div className={styles.field}>
            <label>End Time</label>
            <input
              type="time"
              value={endTime}
              onChange={e => setEndTime(e.target.value)}
              required
            />
          </div>

          <div className={styles.field}>
            <label>Excuse</label>
            <select value={excuse} onChange={e => setExcuse(e.target.value)}>
              <option>Personal</option>
              <option>Medical</option>
              <option>Academic</option>
            </select>
          </div>

          <div className={styles.field}>
            <label>Message</label>
            <textarea
              rows={4}
              value={message}
              onChange={e => setMessage(e.target.value)}
              required
            />
          </div>

          <div className={styles.field}>
            <label>Attachment (optional)</label>
            <input type="file" onChange={handleFile} />
            {attachment && <p>Selected file: {attachment.name}</p>}
          </div>

          <button type="submit" className={styles.submitBtn}>
            Submit Request
          </button>
        </form>
      </main>
    </div>
  );
};

export default LeaveRequestForm;
