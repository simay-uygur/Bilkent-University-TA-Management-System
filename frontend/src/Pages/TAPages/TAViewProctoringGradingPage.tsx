import React, { useState, useEffect, useMemo } from 'react';
import BackBut from '../../components/Buttons/BackBut';
import styles from './TAViewProctoringGradingPage.module.css';

// Shape of the JSON you get from the backend for proctoring
interface ExamDto {
    courseCode: string;
    start:  { year: number; month: number; day: number; hour: number; minute: number };
    finish: { year: number; month: number; day: number; hour: number; minute: number };
    examType: string;
}

// Shape of the JSON you get from the backend for grading
interface GradingDto {
    courseCode: string;
    endDate: { year: number; month: number; day: number; hour: number; minute: number };
}

// Row data for proctoring view
interface ProctoringRow {
    key: string;
    date: string;
    course: string;
    examType: string;
    level: 'BS' | 'MS' | 'PhD';
    time: string;
}

// Row data for grading view
interface GradingRow {
    key: string;
    courseCode: string;
    endDate: string;   // 'dd.MM.yyyy'
    endTime: string;   // 'HH.mm'
}

const formatDate = (y: number, m: number, d: number) =>
    `${d.toString().padStart(2,'0')}/${m.toString().padStart(2,'0')}/${y}`;

const TAViewProctoringGradingPage: React.FC = () => {
    const taId = localStorage.getItem('userId');
    const [mode, setMode] = useState<'proctorings' | 'gradings'>('proctorings');

    // Proctoring state
    const [exams, setExams] = useState<ExamDto[]>([]);

    // Grading state
    const [gradingDtos, setGradingDtos] = useState<GradingDto[]>([]);

    // Fetch proctoring exams
    useEffect(() => {
        if (!taId) return;
        fetch(`/api/ta/${taId}/assignedExams`)
            .then(res => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                return res.json() as Promise<ExamDto[]>;
            })
            .then(setExams)
            .catch(err => console.error('Failed to fetch proctoring exams:', err));
    }, [taId]);

    // Fetch grading tasks
    useEffect(() => {
        if (!taId) return;
        fetch(`/api/ta/${taId}/tasks/grading`)
            .then(res => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                return res.json() as Promise<GradingDto[]>;
            })
            .then(setGradingDtos)
            .catch(err => console.error('Failed to fetch grading tasks:', err));
    }, [taId]);

    // Compute proctoring rows
    const proctoringRows = useMemo<ProctoringRow[]>(() => {
        const pad = (n: number) => n.toString().padStart(2, '0');
        return exams.map(exam => {
            const { start, finish, courseCode, examType } = exam;
            const date = formatDate(start.year, start.month, start.day);
            return {
                key:      `${courseCode}-${date}-${examType}`,
                date,
                course:   courseCode,
                examType,
                level:    'BS', // adjust based on examType if needed
                time:     `${pad(start.hour)}:${pad(start.minute)}–${pad(finish.hour)}:${pad(finish.minute)}`
            };
        });
    }, [exams]);

    // Compute grading rows
    const gradingRows = useMemo<GradingRow[]>(() => {
        const pad = (n: number) => n.toString().padStart(2, '0');
        return gradingDtos.map(g => {
            const { endDate, courseCode } = g;
            const d = pad(endDate.day), m = pad(endDate.month), y = endDate.year;
            const dateStr = `${d}.${m}.${y}`;
            const timeStr = `${pad(endDate.hour)}.${pad(endDate.minute)}`;
            return {
                key:         `${courseCode}-${dateStr}`,
                courseCode,
                endDate:     dateStr,
                endTime:     timeStr
            };
        });
    }, [gradingDtos]);

    return (
        <div className={styles.pageWrapper}>
            <div className={styles.mainContainer}>
                <div className={styles.headerRow}>
                    <BackBut to="/ta" />
                    <div className={styles.headerCenter}>
                        <h2>{mode === 'proctorings' ? 'Proctoring Schedule' : 'Grading Schedule'}</h2>
                        <div className={styles.modeToggle}>
                            <button
                                className={`${styles.modeButton} ${mode === 'proctorings' ? styles.active : ''}`}
                                onClick={() => setMode('proctorings')}
                            >Proctorings</button>
                            <button
                                className={`${styles.modeButton} ${mode === 'gradings' ? styles.active : ''}`}
                                onClick={() => setMode('gradings')}
                            >Gradings</button>
                        </div>
                    </div>
                </div>

                <div className={styles.tableWrapper}>
                    {mode === 'proctorings' ? (
                        <table className={styles.scheduleTable}>
                            <thead>
                            <tr>
                                <th>Date</th>
                                <th>Course</th>
                                <th>Type</th>
                                <th>Level</th>
                                <th>Time</th>
                            </tr>
                            </thead>
                            <tbody>
                            {proctoringRows.map(row => (
                                <tr key={row.key}>
                                    <td>{row.date}</td>
                                    <td>{row.course}</td>
                                    <td>{row.examType}</td>
                                    <td>{row.level}</td>
                                    <td>{row.time}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    ) : (
                        <table className={styles.scheduleTable}>
                            <thead>
                            <tr>
                                <th>Course Code</th>
                                <th>End Date</th>
                                <th>End Time</th>
                            </tr>
                            </thead>
                            <tbody>
                            {gradingRows.map(g => (
                                <tr key={g.key}>
                                    <td>{g.courseCode}</td>
                                    <td>{g.endDate}</td>
                                    <td>{g.endTime}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
};

export default TAViewProctoringGradingPage;


/*
// TAViewProctoringPage.tsx
import React, { useState, useMemo } from 'react';
import styles from './TAViewProctoringGradingPage.module.css';
import BackBut from '../../components/Buttons/BackBut';

interface ProctoringTask {
  year: number;
  month: number;
  day: number;
  course: string;
  building: string;
  startTime: string;
  finishTime: string;
  level: 'BS' | 'MS' | 'PhD';
}

interface GradingTask {
  courseCode: string;
  endDate: string;  // 'dd.MM.yyyy'
  endTime: string;  // 'HH.mm'
}

// sample data
const proctoringTasks: ProctoringTask[] = [
  { year: 2025, month: 4, day: 1,  course: 'CS102',   building: 'B-202', startTime: '10:30', finishTime: '12:00', level: 'BS' },
  { year: 2025, month: 4, day: 1,  course: 'Biology', building: 'C-150', startTime: '08:00', finishTime: '10:30', level: 'MS' },
  { year: 2025, month: 4, day: 5,  course: 'Physics', building: 'A-127', startTime: '08:00', finishTime: '10:30', level: 'PhD' },
  { year: 2025, month: 4, day: 9,  course: 'Math101', building: 'D-250', startTime: '10:30', finishTime: '12:00', level: 'BS' },
];

const gradingTasks: GradingTask[] = [
  { courseCode: 'CS-102', endDate: '02.05.2025', endTime: '12.00' },
  { courseCode: 'CS-319', endDate: '04.05.2025', endTime: '15.30' },
  { courseCode: 'CS-223', endDate: '01.05.2025', endTime: '09.45' },
];

const formatDate = (y: number, m: number, d: number) =>
  `${d.toString().padStart(2,'0')}/${m.toString().padStart(2,'0')}/${y}`;

const TAViewProctoringGradingPage: React.FC = () => {
  const [mode, setMode] = useState<'proctorings' | 'gradings'>('proctorings');

  // sort proctorings by date ascending
  const sortedProctorings = useMemo(() => {
    return [...proctoringTasks].sort((a, b) => {
      const da = new Date(a.year, a.month - 1, a.day).getTime();
      const db = new Date(b.year, b.month - 1, b.day).getTime();
      return da - db;
    });
  }, []);

  // sort gradings by parsed endDate ascending
  const sortedGradings = useMemo(() => {
    return [...gradingTasks].sort((a, b) => {
      const [dA, mA, yA] = a.endDate.split('.');
      const [dB, mB, yB] = b.endDate.split('.');
      return new Date(+yA, +mA - 1, +dA).getTime()
           - new Date(+yB, +mB - 1, +dB).getTime();
    });
  }, []);

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.mainContainer}>
        <div className={styles.headerRow}>
          <BackBut to="/ta" />

          <div className={styles.headerCenter}>
            <h2>
              {mode === 'proctorings' ? 'Proctoring Schedule' : 'Grading Schedule'}
            </h2>
            <div className={styles.modeToggle}>
              <button
                className={`${styles.modeButton} ${
                  mode === 'proctorings' ? styles.active : ''
                }`}
                onClick={() => setMode('proctorings')}
              >
                Proctorings
              </button>
              <button
                className={`${styles.modeButton} ${
                  mode === 'gradings' ? styles.active : ''
                }`}
                onClick={() => setMode('gradings')}
              >
                Gradings
              </button>
            </div>
          </div>
        </div>

        <div className={styles.tableWrapper}>
          {mode === 'proctorings' ? (
            <table className={styles.scheduleTable}>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Course</th>
                  <th>Level</th>
                  <th>Time</th>
                  <th>Classroom</th>
                </tr>
              </thead>
              <tbody>
                {sortedProctorings.map((t, i) => (
                  <tr key={i}>
                    <td>{formatDate(t.year, t.month, t.day)}</td>
                    <td>{t.course}</td>
                    <td>{t.level}</td>
                    <td>{`${t.startTime} – ${t.finishTime}`}</td>
                    <td>{t.building}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <table className={styles.scheduleTable}>
              <thead>
                <tr>
                  <th>Course Code</th>
                  <th>End Date</th>
                  <th>End Time</th>
                </tr>
              </thead>
              <tbody>
                {sortedGradings.map((g, i) => (
                  <tr key={i}>
                    <td>{g.courseCode}</td>
                    <td>{g.endDate}</td>
                    <td>{g.endTime}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};

export default TAViewProctoringGradingPage;
*/
