import React, { useState, useEffect } from 'react';

import { useNavigate } from 'react-router-dom';
import { fetchSchedule, fetchAvailableTAs } from '../api';

import styles from './TADashboard.module.css';
import Calendar from './Calendar';

interface ScheduleItem {
  id: string;
  timeRange: string;
  task: string;
  lesson: string;
}
interface TA {
  id: string;
  name: string;
}

export default function TADashboard() {
  const navigate = useNavigate();
  const [date, setDate]           = useState<Date>(new Date());
  const [startTime, setStartTime] = useState<string>('08:00');
  const [schedule, setSchedule]   = useState<ScheduleItem[]>([]);
  const [swapList, setSwapList]   = useState<TA[]>([]);
  const [swapFor, setSwapFor]     = useState<string | null>(null);
  const [filter, setFilter]       = useState<string>('');

  // Fetch real data on mount
  useEffect(() => {
    fetchSchedule()
      .then(r => setSchedule(r.data))
      .catch(console.error);
    fetchAvailableTAs()
      .then(r => setSwapList(r.data))
      .catch(console.error);
  }, []);

  const filteredTAs = swapList.filter(ta =>
    ta.name.toLowerCase().includes(filter.toLowerCase())
  );

  const handleSwap = (taId: string) => {
    console.log(`Swap ${swapFor} with TA ${taId}`);
    setSwapFor(null);
  };

  return (
    <div className={styles.page}>
      <div className={styles.content}>
        {/* Left panel */}
        <div className={styles.leftPanel}>
          <div className={styles.card}>
          <Calendar
             date={date}
              onDateChange ={(d) => setDate(d as Date)}
              startTime={startTime}
              onStartTimeChange={setStartTime}
            />
          {/*   
            <label className={styles.fieldLabel}>Select Date</label>
            <input
              type="date"
              className={styles.dateInput}
              value={date}
              onChange={e => setDate(e.target.value)}
            />
            <label className={styles.fieldLabel}>Start Time</label>
            <input
              type="time"
              className={styles.timeInput}
              value={startTime}
              onChange={e => setStartTime(e.target.value)}
            />
 */}
          </div>
        </div>

        {/* Right panel */}
        <div className={styles.rightPanel}>
          <div className={styles.card}>
            <h2 className={styles.sectionTitle}>Time Table</h2>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Time</th><th>Task</th><th>Lesson</th><th>Actions</th>
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
                    <td>{item.lesson}</td>
                    <td>
                      <button
                        className={styles.swapBtn}
                        onClick={() => setSwapFor(item.id)}
                      >
                        Swap
                      </button>
                      <button
                        className={styles.transferBtn}
                        onClick={() => navigate(`/leave-request/${item.id}`)}
                      >
                        Transfer
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {swapFor && (
              <section className={styles.swapSection}>
                <h3>Swap for assignment #{swapFor}</h3>
                <input
                  type="text"
                  placeholder="Filter TA…"
                  className={styles.filterInput}
                  value={filter}
                  onChange={e => setFilter(e.target.value)}
                />
                <table className={styles.swapTable}>
                  <thead>
                    <tr><th>TA Name</th><th>Action</th></tr>
                  </thead>
                  <tbody>
                    {filteredTAs.map(ta => (
                      <tr key={ta.id}>
                        <td>{ta.name}</td>
                        <td>
                          <button
                            className={styles.selectBtn}
                            onClick={() => handleSwap(ta.id)}
                          >
                            Select
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </section>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

