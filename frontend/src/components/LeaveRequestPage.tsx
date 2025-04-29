// src/components/LeaveRequestsPage.tsx
import React, { useState } from 'react';
import styles from './LeaveRequestsPage.module.css';

interface LeaveRequest {
  id: number;
  taName: string;
  course: string;
  date: string;
  time: string;
  excuse: string;
  message: string;
  attachmentUrl?: string;
}

const initial: LeaveRequest[] = [
  { id:1, taName:'Alice Smith', course:'CS-101', date:'April 10, 2025', time:'08:00–10:00',
    excuse:'Medical Appointment', message:'A lightning struck me.', attachmentUrl:'/att1.pdf' },
  { id:2, taName:'John Doe',    course:'CS-224', date:'April 11, 2025', time:'13:00–15:00',
    excuse:'Family Emergency', message:'Need to attend a family meeting.' },
];

const LeaveRequestsPage: React.FC = () => {
  const [requests, setRequests] = useState<LeaveRequest[]>(initial);

  const respond = (id: number) => setRequests(rs => rs.filter(r => r.id !== id));

  return (
    <div className={styles.pageWrapper}>
      <h1 className={styles.heading}>Pending Leave Requests</h1>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>TA Name</th><th>Course</th><th>Date</th><th>Time</th>
            <th>Excuse</th><th>Message</th><th>Attachment</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {requests.map(r => (
            <tr key={r.id}>
              <td>{r.taName}</td><td>{r.course}</td><td>{r.date}</td><td>{r.time}</td>
              <td>{r.excuse}</td><td>{r.message}</td>
              <td>{r.attachmentUrl
                  ? <a href={r.attachmentUrl} target="_blank" rel="noreferrer">View</a>
                  : 'No File'}
              </td>
              <td className={styles.actions}>
                <button onClick={() => respond(r.id)} className={styles.approve}>Approve</button>
                <button onClick={() => respond(r.id)} className={styles.reject}>Reject</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {requests.length === 0 && <p className={styles.none}>No pending requests.</p>}
    </div>
  );
};

export default LeaveRequestsPage;
