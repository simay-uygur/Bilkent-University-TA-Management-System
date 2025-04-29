import React, { useState } from 'react';
import DepOfNavBar from '../components/NavBarDepartment';
import styles from './DepartmentOffice.module.css';

interface LeaveRequest {
  id: number;
  taName: string;
  course: string;
  proctoringDate: string;
  requestedTime: string;
  excuse: string;
  message: string;
  attachmentUrl?: string;
}

const initialRequests: LeaveRequest[] = [
  {
    id: 1,
    taName: 'Alice Smith',
    course: 'CS-101',
    proctoringDate: 'April 10, 2025',
    requestedTime: '08:00 – 10:00',
    excuse: 'Medical Appointment',
    message: 'A lightning struck to me.',
    attachmentUrl: '/attachments/attachment1.pdf',
  },
  {
    id: 2,
    taName: 'John Doe',
    course: 'CS-224',
    proctoringDate: 'April 11, 2025',
    requestedTime: '13:00 – 15:00',
    excuse: 'Family Emergency',
    message: 'I need to attend a family meeting due to an emergency.',
    // no attachment
  },
];

const DepartmentOffice: React.FC = () => {
  const [requests, setRequests] = useState<LeaveRequest[]>(initialRequests);

  const handleApprove = (id: number) => {
    // TODO: call API to approve, then remove from list
    setRequests(reqs => reqs.filter(r => r.id !== id));
  };

  const handleReject = (id: number) => {
    // TODO: call API to reject, then remove from list
    setRequests(reqs => reqs.filter(r => r.id !== id));
  };

  return (
    <div className={styles.pageWrapper}>
      

      <div className={styles.content}>
        <h1 className={styles.heading}>Pending Leave Requests</h1>

        <div className={styles.card}>
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
              {requests.map(req => (
                <tr key={req.id}>
                  <td>{req.taName}</td>
                  <td>{req.course}</td>
                  <td>{req.proctoringDate}</td>
                  <td>{req.requestedTime}</td>
                  <td>{req.excuse}</td>
                  <td>{req.message}</td>
                  <td>
                    {req.attachmentUrl
                      ? <a href={req.attachmentUrl} target="_blank" rel="noopener noreferrer">View</a>
                      : 'No File'}
                  </td>
                  <td className={styles.actions}>
                    <button
                      className={styles.approve}
                      onClick={() => handleApprove(req.id)}
                    >
                      Approve
                    </button>
                    <button
                      className={styles.reject}
                      onClick={() => handleReject(req.id)}
                    >
                      Reject
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {requests.length === 0 && (
            <p className={styles.noRequests}>No pending requests.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default DepartmentOffice;
