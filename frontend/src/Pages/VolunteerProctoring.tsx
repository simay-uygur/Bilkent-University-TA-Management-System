import React, { useEffect, useState } from 'react';
import { fetchVolunteerRequests, VolunteerRequest, assignVolunteer } from '../api';
import styles from './VolunteerProctoring.module.css';

export default function VolunteerProctoring() {
  const [requests, setRequests] = useState<VolunteerRequest[]>([]);
  const [page, setPage] = useState(1);

  useEffect(() => {
    // replace with real API call
    fetchVolunteerRequests(page).then(r => setRequests(r.data));
  }, [page]);

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1>Volunteer Proctoring</h1>
        <table className={styles.table}>
          <thead>
            <tr>
              <th></th>
              <th>Course</th>
              <th>TA Needed</th>
              <th>Closes At</th>
              <th>Assign</th>
            </tr>
          </thead>
          <tbody>
            {requests.map(r => (
              <tr key={r.id}>
                <td><input type="checkbox" /></td>
                <td>{r.course} {r.priority && <span className={styles.star}>★</span>}</td>
                <td>TA Needed: {r.needed}</td>
                <td>{r.closesAt}</td>
                <td>
                  <button
                    className={r.assigned ? styles.assignedBtn : styles.unassignedBtn}
                    onClick={() => assignVolunteer(r.id)}
                  >
                    {r.assigned ? 'Assigned' : 'Unassigned'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className={styles.pagination}>
          <button disabled={page===1} onClick={()=>setPage(p=>p-1)}>‹ Previous</button>
          <span>Page {page} of 2</span>
          <button onClick={()=>setPage(p=>p+1)}>Next ›</button>
        </div>
      </div>
    </div>
  );
}
