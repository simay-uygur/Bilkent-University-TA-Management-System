// TAMainPage.tsx  (exactly as you originally sent it)
import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './TAMainPage.module.css';

// Interface for the schedule items
type ScheduleItem = {
  type: 'COURSE' | 'LAB' | 'RECITATION';
  name: string;
  code: string;
  building: string;
  section: string;
  days: number[];   // 1=Monday, 2=Tuesday, etc.
  hours: number[];  // 1..12 corresponding to timeSlots
};

type ScheduleTask = {
  type: 'LAB' | 'RECITATION' | 'OFFICE_HOURS';
  name: string;
  code: string;
  building: string;
  section: string;
  days: number[];   // 1=Monday, 2=Tuesday, etc.
  hours: number[];  // 1..12 corresponding to timeSlots
};

const schedule: ScheduleItem[] = [
  {
    type: 'COURSE',
    name: 'Math',
    code: '101',
    building: 'A-127',
    section: '1',
    days: [1, 3, 5],    // Monday, Wednesday, Friday
    hours: [1, 2],       // 08:30-09:20, 09:30-10:20
  },
  {
    type: 'LAB',
    name: 'Physics',
    code: '101',
    building: 'B-251',
    section: '2',
    days: [2, 4],        // Tuesday, Thursday
    hours: [3, 4, 5, 6], // 10:30-11:20, 11:30-12:20, 12:30-13:20, 13:30-14:20
  },
  {
    type: 'RECITATION',
    name: 'CS',
    code: '101',
    building: 'C-333',
    section: '1',
    days: [1, 2, 3],     // Mon, Tue, Wed
    hours: [8],          // 15:30-16:20
  },
];

const scheduleTasks: ScheduleTask[] = [
  {
    type: 'LAB',
    name: 'Chemistry Lab',
    code: 'CHEM-101',
    building: 'D-300',
    section: '1',
    days: [1, 3],
    hours: [2, 3],
  },
  {
    type: 'RECITATION',
    name: 'Math Recitation TA',
    code: 'MATH-101',
    building: 'E-210',
    section: '2',
    days: [2, 4],
    hours: [4],
  },
];

const timeSlots = [
  '08:30-09:20','09:30-10:20','10:30-11:20','11:30-12:20',
  '12:30-13:20','13:30-14:20','14:30-15:20','15:30-16:20',
  '16:30-17:20','17:30-18:20','18:30-19:20','19:30-20:20',
];

const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

function TAMainPage() {
  const navigate = useNavigate();

  const renderContent = (day: number, hour: number) => {
    const items = schedule.filter(item => item.days.includes(day) && item.hours.includes(hour));
    const tasks = scheduleTasks.filter(task => task.days.includes(day) && task.hours.includes(hour))
                               .map(t => ({ ...t, isTask: true }));
<<<<<<< Updated upstream
    const combined = [...items.map(i => ({ ...i, isTask: false })), ...tasks];
=======
    const combined = items.map(i => ({ ...i, isTask: false })).concat(tasks);
>>>>>>> Stashed changes

    if (!combined.length) return null;

    return combined.map((item, idx) => (
      <div key={idx} className={styles[item.type.toLowerCase()]}>
        <strong>
          {item.type} {item.isTask ? '(TA)' : '(Student)'}
        </strong><br />
        {item.name} - {item.code}<br />
        Building: {item.building}<br />
        Section: {item.section}
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
              {days.map((day, i) => (
                <th key={i} className={styles.dayColumn}>{day}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {timeSlots.map((slot, row) => (
              <tr key={row}>
                <td className={styles.timeColumn}>{slot}</td>
                {days.map((_, col) => (
                  <td key={col} className={styles.cell}>
                    {renderContent(col + 1, row + 1)}
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
}

export default TAMainPage;
