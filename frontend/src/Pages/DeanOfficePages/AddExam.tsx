import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import LoadingPage from '../CommonPages/LoadingPage';
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

interface SlotInfoFull {
  totalStudents: number;
  availableClassrooms: SlotInfoResponse[];
}

interface AddExamRequest {
  duration: {
    start: DateInfo;
    finish: DateInfo;
  };
  //courseCode: string;
  type: string;
  examRooms: string[];
  requiredTas: number;
  workload: number;
}

const AddExam: React.FC = () => {
  const navigate = useNavigate();

  // fetched on mount
  const [courses, setCourses] = useState<string[]>([]);
  const [loadingCourses, setLoadingCourses] = useState(false);

  // user inputs
  const [courseCode, setCourseCode] = useState('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');

  // slot info
  const [slotInfo, setSlotInfo] = useState<SlotInfoFull | null>(null);
  const [showSlots, setShowSlots] = useState(false);
  const [loadingClassrooms, setLoadingClassrooms] = useState(false);

  // selection & details
  const [selectedRooms, setSelectedRooms] = useState<string[]>([]);
  const [examType, setExamType] = useState('');
  const [requiredTas, setRequiredTas] = useState<number>(0);
  const [workload, setWorkload] = useState<number>(4);

  // confirm dialog
  const [confirm, setConfirm] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
    
    setLoadingCourses(true);
    
    axios
      .get<{ facultyCode: string }>(`/api/v1/dean-offices/${userId}`)
      .then(res => res.data.facultyCode)
      .then(fac =>
        axios.get(`/api/v1/dean-offices/${fac}/getCourses`)
      )
      .then(res => {
        if (res.data?.courses) {
          // Extract just the course codes
          const courseCodes = res.data.courses.map((c: any) => c.courseCode);
          setCourses(courseCodes);
        }
      })
      .catch(err => {
        console.error('Error fetching courses:', err);
        setCourses([]);
      })
      .finally(() => {
        setLoadingCourses(false);
      });
  }, []);

  const parseDateInfo = (d: string, t: string): DateInfo => {
    const [year, month, day] = d.split('-').map(Number);
    const [hour, minute] = t.split(':').map(Number);
    return { day, month, year, hour, minute };
  };

  // In your fetchSlots function, update it like this:
const fetchSlots = () => {
  if (!(courseCode && date && startTime && endTime)) return;

  setLoadingClassrooms(true);
  setShowSlots(false);
  setSubmitError(null);

  const payload: SlotInfoRequest = {
    start: parseDateInfo(date, startTime),
    finish: parseDateInfo(date, endTime),
  };

  console.log("Sending slot-info request:", payload);

  axios
    .post<SlotInfoFull>(
      `/api/v1/offerings/${courseCode}/exam/slot-info`,
      payload
    )
    .then(res => {
      console.log("Received classroom data:", res.data);
      
      if (!res.data || !res.data.availableClassrooms || res.data.availableClassrooms.length === 0) {
        setSubmitError("No available classrooms found for the selected time slot. Please try a different time or date.");
        setShowSlots(false);
      } else {
        setSlotInfo(res.data);
        setSelectedRooms([]);
        setShowSlots(true);
      }
    })
    .catch(err => {
      console.error('slot-info error:', err);
      
      let errorMessage = "Failed to fetch available classrooms. Please try again.";
      
      // Check for specific error types
      if (err.response) {
        if (err.response.status === 404) {
          errorMessage = "Course not found or no classroom data available.";
        } else if (err.response.status === 400) {
          errorMessage = "Invalid time or date format. Please check your inputs.";
        } else if (err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        }
      } else if (err.request && !err.response) {
        errorMessage = "Network error. Please check your connection and try again.";
      }
      
      setSubmitError(errorMessage);
    })
    .finally(() => {
      setLoadingClassrooms(false);
    });
};

  const toggleRoom = (id: string) => {
    setSelectedRooms(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const sendAddExam = () => {
    if (!slotInfo) return;

    setSubmitting(true);

    // Format the request body according to the expected structure
    const body: AddExamRequest = {
      duration: {
        start: parseDateInfo(date, startTime),
        finish: parseDateInfo(date, endTime),
      },
      //courseCode: courseCode,
      type: examType,
      examRooms: selectedRooms,
      requiredTas: requiredTas,
      workload: workload,
    };

    // Debug log the payload
    console.log("Sending exam data:", body);

    axios
      .post(`/api/v1/offerings/${courseCode}/add-exam`, body)
      .then(() => {
        console.log("Exam added successfully");
        navigate('/deans-office/view-add-exams');
      })
      .catch(err => {
        console.error('Error adding exam:', err);
        setSubmitError("Failed to add exam. Please check your inputs and try again.");
        setConfirm(false);
      })
      .finally(() => {
        setSubmitting(false);
      });
  };


  // Keep all your existing state variables

  // Rest of your component logic (fetchSlots, toggleRoom, sendAddExam, etc.)
  
  // Remove the full-page loading return:
  // if (loadingCourses) {
  //   return <LoadingPage/>;
  // }

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={() => navigate('/deans-office/view-add-exams')} />
        <h1 className={styles.title}>Add Exam</h1>
      </div>

      <div className={styles.formSection}>
        {loadingCourses ? (
          <div className={styles.sectionLoading}>
            <div className={styles.spinner}></div>
            <p>Loading courses...</p>
          </div>
        ) : (
          <>
            <label>Course</label>
            <select
              value={courseCode}
              onChange={e => setCourseCode(e.target.value)}
              className={styles.selectInput}
            >
              <option value="">-- select --</option>
              {courses.map(c => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>

            <label>Date</label>
            <input
              type="date"
              value={date}
              onChange={e => setDate(e.target.value)}
              className={styles.textInput}
            />

            <label>Start Time</label>
            <input
              type="time"
              value={startTime}
              onChange={e => setStartTime(e.target.value)}
              className={styles.textInput}
            />

            <label>End Time</label>
            <input
              type="time"
              value={endTime}
              onChange={e => setEndTime(e.target.value)}
              className={styles.textInput}
            />

            <button
              className={styles.showRoomsBtn}
              disabled={!(courseCode && date && startTime && endTime) || loadingClassrooms}
              onClick={fetchSlots}
            >
              {loadingClassrooms ? 'Fetching...' : 'Fetch Available Classrooms'}
            </button>
          </>
        )}
      </div>

      {/* This is the loading indicator that will appear below the form */}
      {loadingClassrooms && (
        <div className={styles.loadingContainer}>
          <div className={styles.spinner}></div>
          <p>Loading classrooms... This may take 30-40 seconds.</p>
          <p className={styles.loadingTip}>We're searching for all available rooms for your exam.</p>
        </div>
      )}

      {/* Rest of your component (classroom list, exam details, etc.) */}
      {showSlots && slotInfo && (
        <div className={styles.roomSection}>
          // Add this where the roomSection is displayed:
{showSlots && slotInfo && (
  <div className={styles.roomSection}>
    <h2>
      Available Classrooms &mdash; Total Students:{' '}
      {slotInfo.totalStudents}
    </h2>

    <ul className={styles.roomList}>
      {slotInfo.availableClassrooms.map(room => (
        <li
          key={room.classroomId}
          className={`${styles.roomItem} ${
            selectedRooms.includes(room.classroomId)
              ? styles.selectedRoom
              : ''
          }`}
          onClick={() => toggleRoom(room.classroomId)}
        >
          {room.classroomId} (exam cap: {room.examCapacity})
        </li>
      ))}
    </ul>

    <label>Exam Type</label>
    <input
      type="text"
      value={examType}
      onChange={e => setExamType(e.target.value)}
      placeholder="e.g., Midterm 1, Final"
      className={styles.textInput}
    />

    <label>Required TAs</label>
    <input
      type="number"
      min={0}
      value={requiredTas}
      onChange={e => setRequiredTas(+e.target.value)}
      className={styles.textInput}
    />

    <label>Workload</label>
    <input
      type="number"
      min={0}
      value={workload}
      onChange={e => setWorkload(+e.target.value)}
      className={styles.textInput}
    />

    <button
      className={styles.saveBtn}
      disabled={selectedRooms.length === 0 || !examType || submitting}
      onClick={() => setConfirm(true)}
    >
      {submitting ? 'Adding...' : 'Add Exam'}
    </button>
  </div>
)}
        </div>
      )}

      {submitError && (
  <div className={styles.errorMessage}>
    <p>{submitError}</p>
    <button 
      className={styles.retryBtn} 
      onClick={fetchSlots}
      disabled={loadingClassrooms}
    >
      Try Again
    </button>
  </div>
)}

      {confirm && (
        <ConPop
          message={`Confirm adding ${examType} exam for ${courseCode}?`}
          onConfirm={sendAddExam}
          onCancel={() => setConfirm(false)}
        />
      )}
    </div>
  );
};
/*   return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={() => navigate('/deans-office/view-add-exams')} />
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
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>

        <label>Date</label>
        <input
          type="date"
          value={date}
          onChange={e => setDate(e.target.value)}
          className={styles.textInput}
        />

        <label>Start Time</label>
        <input
          type="time"
          value={startTime}
          onChange={e => setStartTime(e.target.value)}
          className={styles.textInput}
        />

        <label>End Time</label>
        <input
          type="time"
          value={endTime}
          onChange={e => setEndTime(e.target.value)}
          className={styles.textInput}
        />

        <button
          className={styles.showRoomsBtn}
          disabled={!(courseCode && date && startTime && endTime) || loadingClassrooms}
          onClick={fetchSlots}
        >
          {loadingClassrooms ? 'Fetching...' : 'Fetch Available Classrooms'}
        </button>
      </div>

      {loadingClassrooms && (
        <div className={styles.loadingContainer}>
          <div className={styles.spinner}></div>
          <p>Loading classrooms... This may take 30-40 seconds.</p>
        </div>
      )}

      {showSlots && slotInfo && (
        <div className={styles.roomSection}>
          <h2>
            Available Classrooms &mdash; Total Students:{' '}
            {slotInfo.totalStudents}
          </h2>

          <ul className={styles.roomList}>
            {slotInfo.availableClassrooms.map(room => (
              <li
                key={room.classroomId}
                className={`${styles.roomItem} ${
                  selectedRooms.includes(room.classroomId)
                    ? styles.selectedRoom
                    : ''
                }`}
                onClick={() => toggleRoom(room.classroomId)}
              >
                {room.classroomId} (exam cap: {room.examCapacity})
              </li>
            ))}
          </ul>

          <label>Exam Type</label>
          <input
            type="text"
            value={examType}
            onChange={e => setExamType(e.target.value)}
            placeholder="e.g., Midterm 1, Final"
            className={styles.textInput}
          />

          <label>Required TAs</label>
          <input
            type="number"
            min={0}
            value={requiredTas}
            onChange={e => setRequiredTas(+e.target.value)}
            className={styles.textInput}
          />

          <label>Workload</label>
          <input
            type="number"
            min={0}
            value={workload}
            onChange={e => setWorkload(+e.target.value)}
            className={styles.textInput}
          />

          <button
            className={styles.saveBtn}
            disabled={selectedRooms.length === 0 || !examType || submitting}
            onClick={() => setConfirm(true)}
          >
            {submitting ? 'Adding...' : 'Add Exam'}
          </button>
        </div>
      )}

      {submitError && (
        <div className={styles.errorMessage}>
          {submitError}
        </div>
      )}

      {confirm && (
        <ConPop
          message={`Confirm adding ${examType} exam for ${courseCode}?`}
          onConfirm={sendAddExam}
          onCancel={() => setConfirm(false)}
        />
      )}
    </div>
  );
}; */

export default AddExam;
/* //src/pages/AddExam/AddExam.tsx 
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

interface SlotInfoFull {
  totalStudents: number;
  availableClassrooms: SlotInfoResponse[];
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

const AddExam: React.FC = () => {
  const navigate = useNavigate();

  // fetched on mount
  const [courses, setCourses] = useState<string[]>([]);

  // user inputs
  const [courseCode, setCourseCode] = useState('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');

  // slot info
  const [slotInfo, setSlotInfo] = useState<SlotInfoFull | null>(null);
  const [showSlots, setShowSlots] = useState(false);

  // selection & details
  const [selectedRooms, setSelectedRooms] = useState<string[]>([]);
  const [examType, setExamType] = useState('');
  const [requiredTas, setRequiredTas] = useState<number>(0);
  const [workload, setWorkload] = useState<number>(4);

  // confirm dialog
  const [confirm, setConfirm] = useState(false);

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
    axios
      .get<{ facultyCode: string }>(`/api/v1/dean-offices/${userId}`)
      .then(res => res.data.facultyCode)
      .then(fac =>
        axios.get(`/api/v1/dean-offices/${fac}/getCourses`)
      )
      .then(res => {
        if (res.data?.courses) {
          setCourses(res.data.courses.map((c: any) => c.courseCode));
        }
      })
      .catch(err => {
        console.error('Error fetching courses:', err);
        setCourses([]);
      });
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

    axios
      .post<SlotInfoFull>(
        `/api/v1/offerings/${courseCode}/exam/slot-info`,
        payload
      )
      .then(res => {
        setSlotInfo(res.data);
        setSelectedRooms([]);
        setShowSlots(true);
      })
      .catch(err => console.error('slot-info error:', err));
  };

  const toggleRoom = (id: string) => {
    setSelectedRooms(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const sendAddExam = () => {
    if (!slotInfo) return;

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

    axios
      .post(`/api/v1/offerings/${courseCode}/add-exam`, body)
      .then(() => navigate('/deans-office/view-add-exams'))
      .catch(err => console.error('add-exam error:', err))
      .finally(() => setConfirm(false));
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={() => navigate('/deans-office/view-add-exams')} />
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
            <option key={c} value={c}>
              {c}
            </option>
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

      {showSlots && slotInfo && (
        <div className={styles.roomSection}>
          <h2>
            Available Classrooms &mdash; Total Students:{' '}
            {slotInfo.totalStudents}
          </h2>

          <ul className={styles.roomList}>
            {slotInfo.availableClassrooms.map(room => (
              <li
                key={room.classroomId}
                className={`${styles.roomItem} ${
                  selectedRooms.includes(room.classroomId)
                    ? styles.selectedRoom
                    : ''
                }`}
                onClick={() => toggleRoom(room.classroomId)}
              >
                {room.classroomId} (exam cap: {room.examCapacity})
              </li>
            ))}
          </ul>

          <label>Exam Type</label>
          <input
            type="text"
            value={examType}
            onChange={e => setExamType(e.target.value)}
            className={styles.textInput}
          />

          <label>Required TAs</label>
          <input
            type="number"
            min={0}
            value={requiredTas}
            onChange={e => setRequiredTas(+e.target.value)}
            className={styles.textInput}
          />

          <label>Workload</label>
          <input
            type="number"
            min={0}
            value={workload}
            onChange={e => setWorkload(+e.target.value)}
            className={styles.textInput}
          />

          <button
            className={styles.saveBtn}
            disabled={selectedRooms.length === 0 || !examType}
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

export default AddExam; */

/* /* src/pages/AddExam/AddExam.tsx *
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

// individual classroom
interface SlotInfoResponse {
  classroomId: string;
  classCapacity: number;
  examCapacity: number;
}

// full response from slot-info
interface SlotInfoFull {
  totalStudents: number;
  availableClassrooms: SlotInfoResponse[];
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

  // --- fetched on mount
  const [courses, setCourses] = useState<string[]>([]);

  // --- user inputs
  const [courseCode, setCourseCode] = useState('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');

  // --- slot info
  const [slotInfo, setSlotInfo] = useState<SlotInfoFull | null>(null);
  const [showSlots, setShowSlots] = useState(false);

  // --- selection & details
  const [selectedRooms, setSelectedRooms] = useState<string[]>([]);
  const [examType, setExamType] = useState('');
  const [requiredTas, setRequiredTas] = useState<number>(0);
  const [workload, setWorkload] = useState<number>(4);

  // --- confirm dialog
  const [confirm, setConfirm] = useState(false);

  // fetch your courses on mount
  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
    axios
      .get<{ facultyCode: string }>(`/api/v1/dean-offices/${userId}`)
      .then(res => res.data.facultyCode)
      .then(fac => axios.get(`/api/v1/dean-offices/${fac}/getCourses`))
      .then(res => {
        if (res.data?.courses) {
          setCourses(res.data.courses.map((c: any) => c.courseCode));
        }
      })
      .catch(err => {
        console.error('Error fetching courses:', err);
        setCourses([]);
      });
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

    axios
      .post<SlotInfoFull>(
        `/api/v1/offerings/${courseCode}/exam/slot-info`,
        payload
      )
      .then(res => {
        setSlotInfo(res.data);
        setSelectedRooms([]);     // reset any previous picks
        setShowSlots(true);
      })
      .catch(err => console.error('slot-info error:', err));
  };

  const toggleRoom = (id: string) => {
    setSelectedRooms(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const sendAddExam = () => {
    if (!slotInfo) return;
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

    axios
      .post(`/api/v1/offerings/${courseCode}/add-exam`, body)
      .then(() => navigate('/deans-office/view-add-exams'))
      .catch(err => console.error('add-exam error:', err))
      .finally(() => setConfirm(false));
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.header}>
        <BackBut onClick={() => navigate('/deans-office/view-add-exams')} />
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
            <option key={c} value={c}>
              {c}
            </option>
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

     
      
      {showSlots && slotInfo && (
        <div className={styles.roomSection}>
          <h2>
            Available Classrooms &mdash; Total Students:{' '}
            {slotInfo.totalStudents}
          </h2>

          <ul className={styles.roomList}>
            {slotInfo.availableClassrooms.map(room => (
              <li
                key={room.classroomId}
                className={`${styles.roomItem} ${
                  selectedRooms.includes(room.classroomId)
                    ? styles.selectedRoom
                    : ''
                }`}
                onClick={() => toggleRoom(room.classroomId)}
              >
                {room.classroomId} (exam cap: {room.examCapacity})
              </li>
            ))}
          </ul>

          <label>Exam Type</label>
          <input
            type="text"
            value={examType}
            onChange={e => setExamType(e.target.value)}
            className={styles.textInput}
          />

          <label>Required TAs</label>
          <input
            type="number"
            min={0}
            value={requiredTas}
            onChange={e => setRequiredTas(+e.target.value)}
            className={styles.textInput}
          />

          <label>Workload</label>
          <input
            type="number"
            min={0}
            value={workload}
            onChange={e => setWorkload(+e.target.value)}
            className={styles.textInput}
          />

          <button
            className={styles.saveBtn}
            disabled={selectedRooms.length === 0 || !examType}
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

export default AddExam; */

/* src/pages/AddExam/AddExam.tsx 
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
      `/api/v1/dean-offices/${userId}`
    )
    .then(res => axios.get(
    `/api/v1/dean-offices/${res.data.facultyCode}/getCourses`
  ))
  .then(res => {
    // Extract course codes from the response
    if (res.data && res.data.courses) {
      // Map the courses array to get just the course codes
      const courseCodes = res.data.courses.map((course: any) => course.courseCode);
      setCourses(courseCodes);
    } else {
      console.error('Unexpected API response format:', res.data);
      setCourses([]);
    }
  })
  .catch(err => {
    console.error('Error fetching courses:', err);
    setCourses([]);
  });
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
      `/api/v1/offerings/${courseCode}/exam/slot-info`,
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
      `/api/v1/offerings/${courseCode}/add-exam`,
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

export default AddExam; */