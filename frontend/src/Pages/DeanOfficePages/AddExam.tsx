/* src/pages/AddExam/AddExam.tsx */
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AddExam.module.css';

// Mock data; replace with API calls
const availableCourses = [
  'CS-101', 'CS-115', 'CS-319', 'MATH-201', 'PHYS-301'
];

const mockClassrooms = [
  'A-101', 'A-102', 'B-201', 'C-301', 'Lab-1'
];

const AddExam: React.FC = () => {
  const navigate = useNavigate();
  const [courseCode, setCourseCode] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [showRooms, setShowRooms] = useState(false);
  const [availableRooms, setAvailableRooms] = useState<string[]>([]);
  const [selectedRoom, setSelectedRoom] = useState('');
  const [showSaveConfirm, setShowSaveConfirm] = useState(false);

  // Enable room list when user clicks the button and all fields are filled
  const canShowRooms = !!(courseCode && description && date && startTime && endTime);

  const handleShowRooms = () => {
    if (!canShowRooms) return;
    setAvailableRooms(mockClassrooms); // TODO: fetch real availability
    setShowRooms(true);
  };

  const handleBack = () => navigate(-1);

  const handleSubmit = () => setShowSaveConfirm(true);

  const onConfirmSave = () => {
    // TODO: send { courseCode, description, date, startTime, endTime, selectedRoom }
    console.log('Saving exam:', { courseCode, description, date, startTime, endTime, selectedRoom });
    setShowSaveConfirm(false);
    navigate('/deans-office/exams');
  };
  const onCancelSave = () => setShowSaveConfirm(false);

  const filteredCourses = availableCourses.filter(c =>
    c.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={handleBack} />
        <h1 className={styles.title}>Add Exam</h1>
      </div>

      <table className={styles.formTable}>
        <tbody>
          <tr>
            <td><label htmlFor="course-search">Course Code:</label></td>
            <td className={styles.inputCell}>
              <input
                id="course-search"
                type="text"
                placeholder="Search..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
                className={styles.searchInput}
              />
              <select
                value={courseCode}
                onChange={e => setCourseCode(e.target.value)}
                className={styles.selectInput}
              >
                <option value="">-- Select Course --</option>
                {filteredCourses.map(code => (
                  <option key={code} value={code}>{code}</option>
                ))}
              </select>
            </td>
          </tr>
          <tr>
            <td><label htmlFor="desc">Description:</label></td>
            <td>
              <input
                id="desc"
                type="text"
                value={description}
                onChange={e => setDescription(e.target.value)}
                className={styles.textInput}
              />
            </td>
          </tr>
          <tr>
            <td><label htmlFor="date">Date:</label></td>
            <td>
              <input
                id="date"
                type="date"
                value={date}
                onChange={e => setDate(e.target.value)}
                className={styles.textInput}
              />
            </td>
          </tr>
          <tr>
            <td><label htmlFor="start">Start Time:</label></td>
            <td>
              <input
                id="start"
                type="time"
                value={startTime}
                onChange={e => setStartTime(e.target.value)}
                className={styles.textInput}
              />
            </td>
          </tr>
          <tr>
            <td><label htmlFor="end">End Time:</label></td>
            <td>
              <input
                id="end"
                type="time"
                value={endTime}
                onChange={e => setEndTime(e.target.value)}
                className={styles.textInput}
              />
            </td>
          </tr>
        </tbody>
      </table>

      <button
        className={styles.showRoomsBtn}
        onClick={handleShowRooms}
        disabled={!canShowRooms}
      >
        Show Available Classrooms
      </button>

      {showRooms && (
        <div className={styles.roomSection}>
          <h2>Select Available Classroom</h2>
          <ul className={styles.roomList}>
            {availableRooms.map(room => (
              <li
                key={room}
                className={`${styles.roomItem} ${selectedRoom === room ? styles.selectedRoom : ''}`}
                onClick={() => setSelectedRoom(room)}
              >
                {room}
              </li>
            ))}
          </ul>
          <button
            className={styles.saveBtn}
            disabled={!selectedRoom}
            onClick={handleSubmit}
          >
            Save Exam
          </button>
        </div>
      )}

      {showSaveConfirm && (
        <ConPop
          message="Are you sure you want to save this exam?"
          onConfirm={onConfirmSave}
          onCancel={onCancelSave}
        />
      )}
    </div>
  );
};

export default AddExam;
