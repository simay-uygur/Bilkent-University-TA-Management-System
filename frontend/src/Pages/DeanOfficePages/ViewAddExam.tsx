// src/pages/ViewAddExam/ViewAddExam.tsx
import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import styles from './ViewAddExam.module.css';

export interface Exam {
  id: string;
  courseName: string;
  examType: 'Midterm' | 'Final';
  date: string;
  startTime: string;
  endTime: string;
  neededTAs?: number;
  filledTAs?: number;
  classroom: string;
}

const sampleExams: Exam[] = [
  {
    id: 'e1',
    courseName: 'CS101 – Intro to Programming',
    examType: 'Midterm',
    date: '2025-06-01',
    startTime: '10:00',
    endTime: '12:00',
    neededTAs: 5,
    filledTAs: 2,
    classroom: 'A-101',
  },
  {
    id: 'e2',
    courseName: 'MATH201 – Calculus II',
    examType: 'Final',
    date: '2025-06-05',
    startTime: '14:00',
    endTime: '16:00',
    neededTAs: 4,
    filledTAs: 4,
    classroom: 'A-101',
  },
  {
    id: 'e3',
    courseName: 'PHY301 – Physics III',
    examType: 'Midterm',
    date: '2025-06-10',
    startTime: '09:00',
    endTime: '11:00',
    neededTAs: 3,
    filledTAs: 1,
    classroom: 'A-101',
  },
];

const ViewAddExam: React.FC = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const [showError, setShowError] = useState(false);

  const filtered = useMemo(
    () =>
      sampleExams.filter(e =>
        e.courseName.toLowerCase().includes(searchTerm.toLowerCase())
      ),
    [searchTerm]
  );

  const handleAddExams = () => {
    if (!selectedFile) {
      setShowError(true);
    } else {
      setShowConfirm(true);
    }
  };

  const handleExam = () => {
    navigate("/deans-office/add-exam");
  };

  const handleConfirmAdd = () => {
    // TODO: implement actual add-exam logic here
    console.log('Approved adding exams from file:', selectedFile);
    setShowConfirm(false);
    setSelectedFile(null);
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
          placeholder="Search exams by name…"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          className={styles.searchBar}
        />

        <table className={styles.table}>
          <thead>
            <tr>
              <th>Course Name</th>
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
                <td>{exam.courseName}</td>
                <td>{exam.examType}</td>
                <td>{exam.date}</td>
                <td>{exam.startTime}</td>
                <td>{exam.endTime}</td>
                <td>{exam.classroom}</td>
                <td>{exam.neededTAs}</td>
                <td>{exam.filledTAs}</td>
                <td>{exam.neededTAs-exam.filledTAs}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className={styles.actions}>
          <input
            type="file"
            accept=".csv"
            onChange={e => setSelectedFile(e.target.files?.[0] || null)}
            className={styles.fileInput}
          />
          <button onClick={handleAddExams} className={styles.addButton}>
            Add Exams by File
          </button>
        </div>
        <div>
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
