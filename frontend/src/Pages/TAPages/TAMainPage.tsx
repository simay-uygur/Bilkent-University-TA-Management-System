import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './TAMainPage.module.css';

// Backend schedule entry
interface ScheduleEntry {
  date: string;           // ISO date string
  slotIndex: number;      // 1..12
  type: string;
  classroom: string | null;
  code: string | null;
  referenceId: number;
}

const timeSlots = [
  '08:30-09:20','09:30-10:20','10:30-11:20','11:30-12:20',
  '12:30-13:20','13:30-14:20','14:30-15:20','15:30-16:20',
  '16:30-17:20','17:30-18:20','18:30-19:20','19:30-20:20',
];

const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

const TAMainPage: React.FC = () => {
  const navigate = useNavigate();
  const [entries, setEntries] = useState<ScheduleEntry[]>([]);

  useEffect(() => {
    const taId = localStorage.getItem('userId');
    if (!taId) return;
    fetch(`/api/ta/${taId}/schedule`)
      .then(res => res.json())
      .then((data: ScheduleEntry[]) => setEntries(data))
      .catch(err => console.error('Failed to load schedule', err));
  }, []);

  const renderCell = (day: number, hour: number) => {
    const matched = entries.filter(e => {
      const jsDay = new Date(e.date).getDay();
      const entryDay = jsDay === 0 ? 7 : jsDay;
      return entryDay === day && e.slotIndex === hour;
    }).filter(e => e.type !== 'Grading');
  
    if (!matched.length) return null;
  
    return matched.map((e, i) => (
      <div key={i} className={styles[e.type.toLowerCase()]}>
        <strong>{e.type}</strong><br />
        Code: {e.code || '-'}<br />
        Room: {e.classroom || '-'}<br />
      </div>
    ));
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.contentWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th className={styles.timeColumn}>Time</th>
              {daysOfWeek.map((d, idx) => (
                <th key={idx} className={styles.dayColumn}>{d}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {timeSlots.map((slot, rowIdx) => (
              <tr key={rowIdx}>
                <td className={styles.timeColumn}>{slot}</td>
                {daysOfWeek.map((_, colIdx) => (
                  <td key={colIdx} className={styles.cell}>
                    {renderCell(colIdx + 1, rowIdx + 1)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>

        <div className={styles.buttonContainer}>
          <button
            className={styles.button}
            onClick={() => navigate('/ta/monthly-schedule')}
          >
            Go to Proctoring Schedule
          </button>
          <button
            className={styles.button}
            onClick={() => navigate('/ta/view-proctoring')}
          >
            View Proctorings & Grading
          </button>
        </div>
      </div>
    </div>
  );
};

export default TAMainPage;