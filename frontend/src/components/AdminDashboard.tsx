import React, { useEffect, useState } from 'react';
import {
  fetchLeaveRequests,
  approveLeaveRequest,
  rejectLeaveRequest,
  LeaveRequest
} from '../api';
import styles from './AdminDashboard.module.css';

const AdminDashboard: React.FC = () => {
  const [requests, setRequests] = useState<LeaveRequest[]>([]);

  useEffect(() => {
    fetchLeaveRequests().then(resp => setRequests(resp.data));
  }, []);

  return (
    <>
      <header className={styles.header}>TA Management – Department Office</header>
      <main className={styles.container}>
        <h2>Pending Leave Requests</h2>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>TA Name</th>
              <th>Course</th>
              <th>Proctoring Date</th>
              <th>Requested Time</th>
              <th>Excuse</th>
              <th>Message</th>
              <th>Attachment</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {requests.map(r => (
              <tr key={r.id}>
                <td>{r.taName}</td>
                <td>{r.course}</td>
                <td>{new Date(r.proctoringDate).toLocaleDateString()}</td>
                <td>{r.requestedTime}</td>
                <td>{r.excuse}</td>
                <td>{r.message}</td>
                <td>
                  {r.attachmentUrl
                    ? <a href={r.attachmentUrl} target="_blank">View</a>
                    : 'No File'}
                </td>
                <td>
                  <button
                    className={styles.btnApprove}
                    onClick={() => approveLeaveRequest(r.id)}
                  >
                    Approve
                  </button>
                  <button
                    className={styles.btnReject}
                    onClick={() => rejectLeaveRequest(r.id)}
                  >
                    Reject
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </main>
    </>
  );
};

export default AdminDashboard;
