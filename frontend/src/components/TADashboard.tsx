import React, { useEffect, useState } from 'react';
import {
  fetchSchedule,
  ScheduleItem,
  fetchAvailableTAs
} from '../api';
import styles from './TADashboard.module.css';

const TADashboard: React.FC = () => {
  const [schedule, setSchedule] = useState<ScheduleItem[]>([]);
  const [swapList, setSwapList] = useState<{id:string;name:string}[]>([]);
  const [filter, setFilter]     = useState<string>('');

  useEffect(() => {
    fetchSchedule().then(r => setSchedule(r.data));
    fetchAvailableTAs().then(r => setSwapList(r.data));
  }, []);

  const filtered = swapList.filter(ta =>
    ta.name.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <>
      <header className={styles.header}>TA Management</header>
      <main className={styles.container}>
        <section className={styles.section}>
          <h2>Time Table</h2>
          <table className={styles.table}>
            <thead>
              <tr><th>Time</th><th>Task</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {schedule.map(s => (
                <tr key={s.id}>
                  <td>{s.timeRange}</td>
                  <td>{s.task}</td>
                  <td>
                    {s.task === 'Proctoring' && <>
                      <button>Swap</button>
                      <button>Transfer</button>
                    </>}
                    {s.task === 'Leave Request' && <button>View</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>

        <section className={styles.section}>
          <h2>Select TA to Swap</h2>
          <input
            type="text"
            placeholder="Filter TA…"
            value={filter}
            onChange={e => setFilter(e.target.value)}
          />
          <table className={styles.table}>
            <thead><tr><th>TA Name</th><th>Action</th></tr></thead>
            <tbody>
              {filtered.map(ta => (
                <tr key={ta.id}>
                  <td>{ta.name}</td>
                  <td><button>Select</button></td>
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
