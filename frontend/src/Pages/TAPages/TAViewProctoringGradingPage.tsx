import React, { useState, useEffect, useMemo } from 'react';
import { useParams }           from 'react-router-dom';
import BackBut                  from '../../components/Buttons/BackBut';
import styles                   from './TAViewProctoringGradingPage.module.css';

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
  endDate: string;   // 'dd.MM.yyyy'
  endTime: string;   // 'HH.mm'
}

interface ExamRoomDto {
  classroomId: string;
  examCapacity: number;
}

interface ExamDto {
  examId: number;
  duration: {
    start: { year: number; month: number; day: number; hour: number; minute: number };
    finish:{ year: number; month: number; day: number; hour: number; minute: number };
  };
  description: string;
  // optional: courseLevel?: 'BS'|'MS'|'PhD';
  rooms: ExamRoomDto[];
}

const formatDate = (y: number, m: number, d: number) =>
    `${d.toString().padStart(2,'0')}/${m.toString().padStart(2,'0')}/${y}`;

const TAViewProctoringGradingPage: React.FC = () => {
  const { taId } = useParams<{ taId: string }>();
  const [mode, setMode] = useState<'proctorings'|'gradings'>('proctorings');
  const [proctoringTasks, setProctoringTasks] = useState<ProctoringTask[]>([]);
  const [gradingTasks,   setGradingTasks]   = useState<GradingTask[]>([]);

  useEffect(() => {
    if (!taId) return;
    fetch(`/api/ta/${taId}/assignedExams`)
        .then(res => res.json() as Promise<ExamDto[]>)
        .then(exams => {
          const pad = (n: number) => n.toString().padStart(2,'0');

          const pros: ProctoringTask[] = exams.flatMap(exam =>
              exam.rooms.map(room => {
                const { start, finish } = exam.duration;
                return {
                  year:  start.year,
                  month: start.month,
                  day:   start.day,
                  course:    exam.description,
                  building:  room.classroomId,
                  startTime:  `${pad(start.hour)}:${pad(start.minute)}`,
                  finishTime: `${pad(finish.hour)}:${pad(finish.minute)}`,
                  level:      'BS' // or pull from exam.courseLevel
                };
              })
          );

          const grads: GradingTask[] = exams.map(exam => {
            const { finish } = exam.duration;
            const d = pad(finish.day), m = pad(finish.month), y = finish.year;
            return {
              courseCode: exam.description,
              endDate:    `${d}.${m}.${y}`,
              endTime:    `${pad(finish.hour)}.${pad(finish.minute)}`
            };
          });

          setProctoringTasks(pros);
          setGradingTasks(grads);
        })
        .catch(console.error);
  }, [taId]);

  const sortedProctorings = useMemo(() =>
      [...proctoringTasks].sort((a,b)=>{
        return new Date(a.year,a.month-1,a.day).getTime()
            - new Date(b.year,b.month-1,b.day).getTime();
      }), [proctoringTasks]
  );

  const sortedGradings = useMemo(() =>
      [...gradingTasks].sort((a,b)=>{
        const [dA,mA,yA]=a.endDate.split('.');
        const [dB,mB,yB]=b.endDate.split('.');
        return new Date(+yA,+mA-1,+dA).getTime()
            - new Date(+yB,+mB-1,+dB).getTime();
      }), [gradingTasks]
  );

  return (
      <div className={styles.pageWrapper}>
        <div className={styles.mainContainer}>
          <div className={styles.headerRow}>
            <BackBut to="/ta" />
            <div className={styles.headerCenter}>
              <h2>{mode==='proctorings' ? 'Proctoring Schedule' : 'Grading Schedule'}</h2>
              <div className={styles.modeToggle}>
                <button
                    className={`${styles.modeButton} ${mode==='proctorings'?styles.active:''}`}
                    onClick={()=>setMode('proctorings')}
                >Proctorings</button>
                <button
                    className={`${styles.modeButton} ${mode==='gradings'?styles.active:''}`}
                    onClick={()=>setMode('gradings')}
                >Gradings</button>
              </div>
            </div>
          </div>

          <div className={styles.tableWrapper}>
            {mode==='proctorings' ? (
                <table className={styles.scheduleTable}>
                  <thead>
                  <tr><th>Date</th><th>Course</th><th>Level</th><th>Time</th><th>Classroom</th></tr>
                  </thead>
                  <tbody>
                  {sortedProctorings.map((t,i)=>(
                      <tr key={`${t.year}${t.month}${t.day}${t.building}`}>
                        <td>{formatDate(t.year,t.month,t.day)}</td>
                        <td>{t.course}</td>
                        <td>{t.level}</td>
                        <td>{`${t.startTime}–${t.finishTime}`}</td>
                        <td>{t.building}</td>
                      </tr>
                  ))}
                  </tbody>
                </table>
            ) : (
                <table className={styles.scheduleTable}>
                  <thead>
                  <tr><th>Course Code</th><th>End Date</th><th>End Time</th></tr>
                  </thead>
                  <tbody>
                  {sortedGradings.map((g,i)=>(
                      <tr key={`${g.courseCode}-${g.endDate}`}>
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
