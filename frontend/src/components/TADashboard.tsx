
// import React, { useEffect, useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import {
//   fetchSchedule,
//   ScheduleItem,
//   fetchAvailableTAs
// } from '../api';
// import styles from './TADashboard.module.css';

// const TADashboard: React.FC = () => {
//   const [schedule, setSchedule]       = useState<ScheduleItem[]>([]);
//   const [swapList, setSwapList]       = useState<{id:string;name:string}[]>([]);
//   const [filter, setFilter]           = useState<string>('');
//   const [loadingSchedule, setLoading] = useState<boolean>(true);
//   const navigate                       = useNavigate();

//   useEffect(() => {
//     // 1) Load schedule from your database via API
//     fetchSchedule()
//       .then(r => setSchedule(r.data))
//       .catch((err => setError(err.message)) => {
//         /* you may want to show an error here */
//       })
//       .finally(() => setLoading(false));

//     // 2) Load swap‑eligible TAs
//     fetchAvailableTAs().then(r => setSwapList(r.data));
//   }, []);

//   const filtered = swapList.filter(ta =>
//     ta.name.toLowerCase().includes(filter.toLowerCase())
//   );

//   return (
//     <>
//       <header className={styles.header}>TA Management</header>
//       <main className={styles.container}>
//         <section className={styles.section}>
//           <h2>Time Table</h2>

//           {loadingSchedule ? (
//             <p>Loading schedule…</p>
//           ) : schedule.length === 0 ? (
//             <p className={styles.emptyState}>
//               No assignments found. Make sure your schedule data is populated in the database and that <code>fetchSchedule()</code> is pointing to the correct endpoint.
//             </p>
//           ) : (
//             <table className={styles.table}>
//               <thead>
//                 <tr>
//                   <th>Time</th>
//                   <th>Task</th>
//                   <th>Actions</th>
//                 </tr>
//               </thead>
//               <tbody>
//                 {schedule.map(s => (
//                   <tr key={s.id}>
//                     <td>{s.timeRange}</td>
//                     <td>
//                       <button
//                         className={styles.linkCell}
//                         onClick={() => navigate(`/leave-request/${s.id}`)}
//                       >
//                         {s.task}
//                       </button>
//                     </td>
//                     <td>
//                       <button
//                         className={styles.actionBtn}
//                         onClick={() => navigate(`/leave-request/${s.id}`)}
//                       >
//                         Request Leave
//                       </button>
//                     </td>
//                   </tr>
//                 ))}
//               </tbody>
//             </table>
//           )}
//         </section>

//         {/* ... Swap‑TA section remains unchanged ... */}
//       </main>
//     </>
//   );
// };

// export default TADashboard;
// src/components/TADashboard.tsx

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './TADashboard.module.css';

interface ScheduleItem {
  id: string;
  timeRange: string;
  task: string;
}

// pre‑populated mock tasks for convenience
const mockSchedule: ScheduleItem[] = [
  { id: '1', timeRange: '08:00 – 10:00', task: 'Proctoring – CS101' },
  { id: '2', timeRange: '10:00 – 12:00', task: 'Lecture Assist – CS315' },
  { id: '3', timeRange: '13:00 – 15:00', task: 'Lab Supervision – CS224' },
  { id: '4', timeRange: '15:00 – 17:00', task: 'Office Hours' },
];

const TADashboard: React.FC = () => {
  const [schedule] = useState<ScheduleItem[]>(mockSchedule);
  const navigate   = useNavigate();

  return (
    <>
    
      <header className={styles.header}>TA Management</header>
      <main className={styles.container}>
        <section className={styles.section}>
          <h2>Time Table</h2>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Time</th>
                <th>Task</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {schedule.map(item => (
                <tr key={item.id}>
                  <td>{item.timeRange}</td>
                  <td>
                    <button
                      className={styles.linkCell}
                      onClick={() => navigate(`/leave-request/${item.id}`)}
                    >
                      {item.task}
                    </button>
                  </td>
                  <td>
                    <button
                      className={styles.actionBtn}
                      onClick={() => navigate(`/leave-request/${item.id}`)}
                    >
                      Request Leave
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </main>
    </>
  );
};

export default TADashboard;
