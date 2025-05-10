/* src/pages/AddExam/AddExam.tsx */
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AddExam.module.css';

interface DateInfo {
  day: number;
  month: number;
  year: number;
  hour: number;
  minute: number;
}

interface SlotInfoRequest {
  start: DateInfo;
  finish: DateInfo;
}

interface SlotInfoResponse {
  classroomId: string;
  classCapacity: number;
  examCapacity: number;
}

interface AddExamRequest {
  duration: {
    start: DateInfo;
    finish: DateInfo;
  };
  type: string;
  examRooms: string[];
  requiredTas: number;
  workload: number;
}

export const AddExam: React.FC = () => {
  const navigate = useNavigate();

  // fetched on mount
  const [courses, setCourses] = useState<string[]>([]);
  // user inputs
  const [courseCode, setCourseCode] = useState('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  // slot info
  const [slots, setSlots] = useState<SlotInfoResponse[]>([]);
  const [showSlots, setShowSlots] = useState(false);
  // selection & details
  const [selectedRooms, setSelectedRooms] = useState<string[]>([]);
  const [examType, setExamType] = useState('');
  const [requiredTas, setRequiredTas] = useState<number>(0);  // default 0
  const [workload, setWorkload] = useState<number>(4);      // default 4
  // confirm
  const [confirm, setConfirm] = useState(false);

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
    // fetch dean-office then courses
    axios.get<{ facultyCode: string }>(
      `http://localhost:8080/api/v1/dean-offices/${userId}`
    )
    .then(res => axios.get<string[]>(
      `http://localhost:8080/api/v1/dean-offices/${res.data.facultyCode}/getCourses`
    ))
    .then(res => setCourses(res.data))
    .catch(err => console.error(err));
  }, []);

  const parseDateInfo = (d: string, t: string): DateInfo => {
    const [year, month, day] = d.split('-').map(Number);
    const [hour, minute] = t.split(':').map(Number);
    return { day, month, year, hour, minute };
  };

  const fetchSlots = () => {
    if (!(courseCode && date && startTime && endTime)) return;
    const payload: SlotInfoRequest = {
      start: parseDateInfo(date, startTime),
      finish: parseDateInfo(date, endTime),
    };
    axios.post<SlotInfoResponse[]>(
      `http://localhost:8080/api/v1/offerings/${courseCode}/exam/slot-info`,
      payload
    )
    .then(res => {
      setSlots(res.data);
      setShowSlots(true);
    })
    .catch(err => console.error(err));
  };

  const toggleRoom = (id: string) => {
    setSelectedRooms(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const sendAddExam = () => {
    const body: AddExamRequest = {
      duration: {
        start: parseDateInfo(date, startTime),
        finish: parseDateInfo(date, endTime),
      },
      type: examType,
      examRooms: selectedRooms,
      requiredTas,
      workload,
    };
    axios.post(
      `http://localhost:8080/api/v1/offerings/${courseCode}/add-exam`,
      body
    )
    .then(() => navigate('/deans-office/view-add-exams'))
    .catch(err => console.error(err))
    .finally(() => setConfirm(false));
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={() => navigate("/deans-office/view-add-exams")} />
        <h1 className={styles.title}>Add Exam</h1>
      </div>

      <div className={styles.formSection}>
        <label>Course</label>
        <select
          value={courseCode}
          onChange={e => setCourseCode(e.target.value)}
          className={styles.selectInput}
        >
          <option value="">-- select --</option>
          {courses.map(c => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>

        <label>Date</label>
        <input
          type="date"
          value={date}
          onChange={e => setDate(e.target.value)}
          className={styles.textInput}
        />

        <label>Start</label>
        <input
          type="time"
          value={startTime}
          onChange={e => setStartTime(e.target.value)}
          className={styles.textInput}
        />

        <label>End</label>
        <input
          type="time"
          value={endTime}
          onChange={e => setEndTime(e.target.value)}
          className={styles.textInput}
        />

        <button
          className={styles.showRoomsBtn}
          disabled={!(courseCode && date && startTime && endTime)}
          onClick={fetchSlots}
        >
          Fetch Available Classrooms
        </button>
      </div>

      {showSlots && (
        <div className={styles.roomSection}>
          <h2>Select Rooms</h2>
          <ul className={styles.roomList}>
            {slots.map(s => (
              <li
                key={s.classroomId}
                className={`${styles.roomItem} ${
                  selectedRooms.includes(s.classroomId)
                    ? styles.selectedRoom
                    : ''
                }`}
                onClick={() => toggleRoom(s.classroomId)}
              >
                {s.classroomId} (cap {s.examCapacity})
              </li>
            ))}
          </ul>

          <label>Type</label>
          <input
            type="text"
            value={examType}
            onChange={e => setExamType(e.target.value)}
            className={styles.textInput}
          />

          <button
            className={styles.saveBtn}
            disabled={selectedRooms.length === 0}
            onClick={() => setConfirm(true)}
          >
            Add Exam
          </button>
        </div>
      )}

      {confirm && (
        <ConPop
          message="Confirm adding this exam?"
          onConfirm={sendAddExam}
          onCancel={() => setConfirm(false)}
        />
      )}
    </div>
  );
};

export default AddExam;