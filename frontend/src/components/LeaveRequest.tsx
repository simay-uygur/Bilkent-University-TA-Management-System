// src/components/LeaveRequestForm.tsx
import React, { useEffect, useState, FormEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  fetchScheduleItem,
  ScheduleItem,
  submitLeaveRequest,
  LeaveRequestPayload
} from '../api';
import styles from './LeaveRequestForm.module.css';

const LeaveRequestForm: React.FC = () => {
  const { scheduleId } = useParams<{ scheduleId: string }>();
  const navigate       = useNavigate();

  const [item, setItem]       = useState<ScheduleItem | null>(null);
  const [startTime, setStartTime] = useState<string>('');
  const [endTime, setEndTime]     = useState<string>('');
  const [excuse, setExcuse]       = useState<string>('Personal');
  const [message, setMessage]     = useState<string>('');
  const [attachment, setAttachment] = useState<File | null>(null);
  const [error, setError]         = useState<string>('');

  useEffect(() => {
    if (scheduleId) {
      fetchScheduleItem(scheduleId).then(r => {
        setItem(r.data);
        // prefill times if you like:
        setStartTime(item?.timeRange.split(' – ')[0] || '');
        setEndTime(item?.timeRange.split(' – ')[1] || '');
      });
    }
  }, [scheduleId]);

  const handleFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    setAttachment(e.target.files?.[0] ?? null);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!scheduleId) return;
    setError('');
    try {
      const payload: LeaveRequestPayload = {
        scheduleId,
        startTime,
        endTime,
        excuse,
        message,
      };
      await submitLeaveRequest(payload, attachment);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message ?? 'Submission failed');
      } else {
        setError((err as Error).message);
      }
    }
  };

  if (!item) {
    return <p className={styles.loading}>Loading…</p>;
  }

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
            <select
              value={excuse}
              onChange={e => setExcuse(e.target.value)}
            >
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
          </div>

          {error && <p className={styles.error}>{error}</p>}

          <button type="submit" className={styles.submitBtn}>
            Submit Request
          </button>
        </form>
      </main>
    </div>
  );
};

export default LeaveRequestForm;
