// src/pages/ViewAddExam/ViewAddExam.tsx
import React, { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import styles from './ViewAddExam.module.css';
import axios from 'axios';
import { format } from 'date-fns';

export interface Exam {
  id: number;
  courseCode: string;
  examType: string;
  date: string;       // yyyy-MM-dd
  startTime: string;  // HH:mm
  endTime: string;    // HH:mm
  neededTAs: number;
  filledTAs: number;  // default 0
  classroom: string;  // first room in array
}

const ViewAddExam: React.FC = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [exams, setExams] = useState<Exam[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const [showError, setShowError] = useState(false);

useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
    // fetch dean-office then courses
    axios.get<{ facultyCode: string }>(
      `http://localhost:8080/api/v1/dean-offices/${userId}`
    )
  }, []);

  // 1) Fetch on mount
  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
  
    const fetchExams = async () => {
      try {
        // 1) get facultyCode from dean-office
        const deanRes = await axios.get<{ facultyCode: string }>(
          `http://localhost:8080/api/v1/dean-offices/${userId}`
        );
        const facultyCode = deanRes.data.facultyCode;
        if (!facultyCode) {
          console.error('No facultyCode returned from /dean-offices');
          return;
        }
  
        // 2) get exams for that facultyCode
        const examsRes = await axios.get<
          {
            examId: number;
            duration: {
              start: { year: number; month: number; day: number; hour: number; minute: number };
              finish: { year: number; month: number; day: number; hour: number; minute: number };
            };
            courseCode: string;
            type: string;
            examRooms: string[];
            requiredTas: number;
          }[]
        >(
          `http://localhost:8080/api/v1/dean-offices/${facultyCode}/exams`
        );
  
        // 3) map into your UI model
        const mapped: Exam[] = examsRes.data.map(e => {
          const { start, finish } = e.duration;
          const dateObj = new Date(start.year, start.month - 1, start.day);
          const startObj = new Date(
            start.year, start.month - 1, start.day,
            start.hour, start.minute
          );
          const endObj = new Date(
            finish.year, finish.month - 1, finish.day,
            finish.hour, finish.minute
          );
          return {
            id: e.examId,
            courseCode: e.courseCode,
            examType: e.type,
            date: format(dateObj, 'yyyy-MM-dd'),
            startTime: format(startObj, 'HH:mm'),
            endTime: format(endObj, 'HH:mm'),
            neededTAs: e.requiredTas,
            filledTAs: 0,
            classroom: e.examRooms[0] ?? '',
          };
        });
  
        setExams(mapped);
      } catch (err) {
        console.error('Failed to load exams', err);
      }
    };
  
    fetchExams();
  }, []);

  // 2) Filter by search term
  const filtered = useMemo(
    () =>
      exams.filter(e =>
        e.courseCode.toLowerCase().includes(searchTerm.toLowerCase())
      ),
    [exams, searchTerm]
  );

  const handleAddExams = () => {
    if (!selectedFile) {
      setShowError(true);
    } else {
      setShowConfirm(true);
    }
  };

  const handleExam = () => {
    navigate('/deans-office');
  };

  const handleConfirmAdd = async () => {
    if (!selectedFile) return;
    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      await axios.post<Map<string, any>>(
        '/api/upload/exams',
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );
      // reload after successful import
      window.location.reload();
    } catch (err) {
      console.error('Failed to import exams', err);
    } finally {
      setShowConfirm(false);
      setSelectedFile(null);
    }
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to="/department-office" />
        <h1 className={styles.title}>Exams</h1>
      </div>

      <div className={styles.container}>
        <input
          type="text"
          placeholder="Search exams by code…"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          className={styles.searchBar}
        />

        <table className={styles.table}>
          <thead>
            <tr>
              <th>Course Code</th>
              <th>Exam Type</th>
              <th>Date</th>
              <th>Start</th>
              <th>End</th>
              <th>Classroom</th>
              <th>TA Needed</th>
              <th>TA Filled</th>
              <th>TA Left</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(exam => (
              <tr key={exam.id}>
                <td>{exam.courseCode}</td>
                <td>{exam.examType}</td>
                <td>{exam.date}</td>
                <td>{exam.startTime}</td>
                <td>{exam.endTime}</td>
                <td>{exam.classroom}</td>
                <td>{exam.neededTAs}</td>
                <td>{exam.filledTAs}</td>
                <td>{exam.neededTAs - exam.filledTAs}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className={styles.actions}>
          <input
            type="file"
            accept=".xlsx"
            onChange={e => setSelectedFile(e.target.files?.[0] || null)}
            className={styles.fileInput}
          />
          <button onClick={handleAddExams} className={styles.addButton}>
            Add Exams by File
          </button>
          <button onClick={handleExam} className={styles.nav}>
            Add Exam
          </button>
        </div>
      </div>

      {showError && (
        <ErrPopUp
          message="Please choose a file before adding exams."
          onConfirm={() => setShowError(false)}
        />
      )}

      {showConfirm && (
        <ConPop
          message="Are you sure you want to add exams from the selected file?"
          onConfirm={handleConfirmAdd}
          onCancel={() => setShowConfirm(false)}
        />
      )}
    </div>
  );
};

export default ViewAddExam;
