// src/pages/TAMonSchPage/TAMonSchPage.tsx
import React, { Fragment, useState } from 'react';
import TANavBar from '../../components/NavBars/TANavBar';
import ConPop from '../../components/PopUp/ConPop';
import BackBut from '../../components/Buttons/BackBut';
import styles from './TAMonSchPage.module.css';

interface ProctoringTask {
  startTime: string;
  finishTime: string;
  course: string;
  courseId: string;
  building: string;
  year: number;
  month: number;
  day: number;
  TASwap?: AvailableTASwap[];
  TARequest?: AvailableTATransfer[];
}

interface AvailableTASwap {
  id: number;
  name: string;
  availableProctoring: ProctoringTask[];
}

interface AvailableTATransfer {
  id: number;
  name: string;
}

const proctoringTasks: ProctoringTask[] = [
  {
    startTime: '08:00',
    finishTime: '10:30',
    course: 'Physics',
    courseId: '101',
    building: 'A-127',
    year: 2025,
    month: 4,
    day: 1,
    TASwap: [
      {
        id: 1,
        name: 'John Doe',
        availableProctoring: [
          {
            startTime: '08:00',
            finishTime: '10:30',
            course: 'Physics',
            courseId: '101',
            building: 'A-127',
            year: 2025,
            month: 4,
            day: 1,
          },
          {
            startTime: '10:30',
            finishTime: '12:00',
            course: 'Math101',
            courseId: '101',
            building: 'D-250',
            year: 2025,
            month: 4,
            day: 1,
          },
        ],
      },
      {
        id: 2,
        name: 'Jane Smith',
        availableProctoring: [
          {
            startTime: '08:00',
            finishTime: '10:30',
            course: 'CS102',
            courseId: '202',
            building: 'B-202',
            year: 2025,
            month: 4,
            day: 1,
          },
          {
            startTime: '10:30',
            finishTime: '12:00',
            course: 'Biology',
            courseId: '303',
            building: 'C-150',
            year: 2025,
            month: 4,
            day: 1,
          },
        ],
      },
    ],
    TARequest: [
      { id: 1, name: 'Emily Johnson' },
      { id: 2, name: 'Mark Brown' },
    ],
  },
  {
    startTime: '10:30',
    finishTime: '12:00',
    course: 'CS102',
    courseId: '202',
    building: 'B-202',
    year: 2025,
    month: 4,
    day: 1,
    TASwap: [
      { id: 1, name: 'Alice Green', availableProctoring: [] },
      { id: 2, name: 'Bob White', availableProctoring: [] },
    ],
    TARequest: [
      { id: 1, name: 'Chad Black' },
      { id: 2, name: 'Sara Yellow' },
    ],
  },
  {
    startTime: '08:00',
    finishTime: '10:30',
    course: 'Biology',
    courseId: '303',
    building: 'C-150',
    year: 2025,
    month: 4,
    day: 5,
    TASwap: [
      { id: 1, name: 'Dan Red', availableProctoring: [] },
      { id: 2, name: 'Elena Blue', availableProctoring: [] },
    ],
    TARequest: [
      { id: 1, name: 'Jill Pink' },
      { id: 2, name: 'Tom Green' },
    ],
  },
  {
    startTime: '10:30',
    finishTime: '12:00',
    course: 'Math101',
    courseId: '101',
    building: 'D-250',
    year: 2025,
    month: 4,
    day: 9,
    TASwap: [
      { id: 1, name: 'Paul Gray', availableProctoring: [] },
      { id: 2, name: 'Kara Gold', availableProctoring: [] },
    ],
    TARequest: [
      { id: 1, name: 'Liam White' },
      { id: 2, name: 'Zoe Silver' },
    ],
  },
];

const getMonthName = (month: number) => {
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  return months[month];
};

const getDaysInMonth = (month: number, year: number) =>
  new Date(year, month + 1, 0).getDate();

const TAMonSchPage: React.FC = () => {
  const [selectedTaskId, setSelectedTaskId] = useState<string | null>(null);
  const [currentMonth, setCurrentMonth] = useState<number>(new Date().getMonth());
  const [currentYear, setCurrentYear] = useState<number>(new Date().getFullYear());
  const [proctoringDetails, setProctoringDetails] = useState<ProctoringTask[]>([]);
  const [highlightedDay, setHighlightedDay] = useState<number | null>(null);

  const [showTransferPopup, setShowTransferPopup] = useState(false);
  const [showSwapPopup, setShowSwapPopup] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [showTransferConfirmation, setShowTransferConfirmation] = useState(false);
  const [showSwapConfirmation, setShowSwapConfirmation] = useState(false);

  const [selectedTA, setSelectedTA] = useState<number | ''>('');
  const [selectedProctoringTask, setSelectedProctoringTask] = useState<string | ''>('');
  const [message, setMessage] = useState('');

  const daysInMonth = getDaysInMonth(currentMonth, currentYear);
  const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay();

  const handleDayClick = (day: number) => {
    const tasks = proctoringTasks.filter(
      t => t.day === day && t.month === currentMonth + 1 && t.year === currentYear
    );
    if (tasks.length) {
      setSelectedTaskId(tasks[0].courseId);
      setHighlightedDay(day);
      setProctoringDetails(tasks);
    }
  };
  const handlePrevMonth = () => {
    if (currentMonth === 0) {
      setCurrentMonth(11);
      setCurrentYear(y => y - 1);
    } else {
      setCurrentMonth(m => m - 1);
    }
    setProctoringDetails([]);
    setHighlightedDay(null);
  };
  const handleNextMonth = () => {
    if (currentMonth === 11) {
      setCurrentMonth(0);
      setCurrentYear(y => y + 1);
    } else {
      setCurrentMonth(m => m + 1);
    }
    setProctoringDetails([]);
    setHighlightedDay(null);
  };

  const openTransfer = () => {
    setShowTransferPopup(true);
    setErrorMessage(null);
  };
  const closeTransfer = () => {
    setShowTransferPopup(false);
    setErrorMessage(null);
    setSelectedTA('');
    setMessage('');
  };
  const handleTransferRequest = () => {
    if (!selectedTA || !message.trim()) {
      setErrorMessage(!selectedTA ? 'Please select a TA' : 'Textfield is empty!');
    } else {
      setShowTransferConfirmation(true);
    }
  };
  const confirmTransfer = () => {
    setShowTransferConfirmation(false);
    closeTransfer();
  };

  const openSwap = () => {
    setShowSwapPopup(true);
    setErrorMessage(null);
  };
  const closeSwap = () => {
    setShowSwapPopup(false);
    setErrorMessage(null);
    setSelectedTA('');
    setSelectedProctoringTask('');
    setMessage('');
  };
  const handleSwapRequest = () => {
    if (!selectedTA || !selectedProctoringTask || !message.trim()) {
      setErrorMessage(
        !selectedTA
          ? 'Please select a TA'
          : !selectedProctoringTask
          ? 'Please select a proctoring task'
          : 'Textfield is empty!'
      );
    } else {
      setShowSwapConfirmation(true);
    }
  };
  const confirmSwap = () => {
    setShowSwapConfirmation(false);
    closeSwap();
  };

  return (
    <div className={styles.pageWrapper}>
      
      <BackBut to = '/ta'/>
      {/* Main content */}
      <div className={styles.mainContainer}>
        {/* Calendar */}
        <div className={styles.calendarContainer}>
          <div className={styles.monthNav}>
            <button className={styles.navButton} onClick={handlePrevMonth}>{'<'}</button>
            <div className={styles.monthYear}>
              {getMonthName(currentMonth)} {currentYear}
            </div>
            <button className={styles.navButton} onClick={handleNextMonth}>{'>'}</button>
          </div>

          <div className={styles.calendar}>
            {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].map((d, i) => (
              <div key={i} className={styles.dayHeader}>{d}</div>
            ))}
            {Array.from({ length: firstDayOfMonth }).map((_, i) => (
              <div key={`e${i}`} className={styles.emptyCell} />
            ))}
            {Array.from({ length: daysInMonth }).map((_, i) => {
              const day = i + 1;
              const hasTask = proctoringTasks.some(
                t => t.day === day && t.month === currentMonth + 1 && t.year === currentYear
              );
              return (
                <div
                  key={day}
                  className={styles.dayCell}
                  onClick={() => hasTask && handleDayClick(day)}
                  style={{
                    backgroundColor: hasTask ? '#4CAF50' : 'transparent',
                    cursor: hasTask ? 'pointer' : 'default',
                    border: highlightedDay === day ? '4px solid #da9fcc' : undefined,
                    width: highlightedDay === day ? 32 : 40,
                    height: highlightedDay === day ? 32 : 40,
                  }}
                >
                  {day}
                </div>
              );
            })}
          </div>
        </div>

        {/* Task table */}
        <div className={styles.scheduleContainer}>
          <h3>Proctoring Tasks</h3>
          <table className={styles.taskTable}>
            <thead>
              <tr>
                <th>Course</th>
                <th>Course ID</th>
                <th>Start</th>
                <th>End</th>
                <th>Building</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {proctoringDetails.map((task, idx) => (
                <tr key={idx}>
                  <td>{task.course}</td>
                  <td>{task.courseId}</td>
                  <td>{task.startTime}</td>
                  <td>{task.finishTime}</td>
                  <td>{task.building}</td>
                  <td className={styles.actionsCell}>
                    <button className={styles.deleteBtn} onClick={openTransfer}>
                      Transfer
                    </button>
                    <button className={styles.greenBtn} onClick={openSwap}>
                      Swap
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Transfer Popup */}
      {showTransferPopup && (
        <div className={styles.popupOverlay}>
          <div className={styles.popup}>
            <h3>Transfer Request</h3>
            <div className={styles.formGroup}>
              <label>Select TA:</label>
              <select
                value={selectedTA}
                onChange={e => setSelectedTA(Number(e.target.value))}
              >
                <option value="">Select TA</option>
                {proctoringTasks
                  .find(t => t.courseId === selectedTaskId)
                  ?.TARequest?.map(ta => (
                    <option key={ta.id} value={ta.id}>{ta.name}</option>
                  ))}
              </select>
            </div>
            <div className={styles.formGroup}>
              <label>Message:</label>
              <textarea
                value={message}
                onChange={e => setMessage(e.target.value)}
                placeholder="Write your message here"
                disabled={!selectedTA}
              />
            </div>
            {errorMessage && (
              <div className={styles.errorMessage}>{errorMessage}</div>
            )}
            <div className={styles.buttonContainer}>
              <button className={styles.approveBtn} onClick={handleTransferRequest}>
                Approve
              </button>
              <button className={styles.cancelBtn} onClick={closeTransfer}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Swap Popup */}
      {showSwapPopup && (
        <div className={styles.popupOverlay}>
          <div className={styles.popup}>
            <h3>Swap Request</h3>
            <div className={styles.formGroup}>
              <label>Select TA:</label>
              <select
                value={selectedTA}
                onChange={e => setSelectedTA(Number(e.target.value))}
              >
                <option value="">Select TA</option>
                {proctoringTasks
                  .find(t => t.courseId === selectedTaskId)
                  ?.TASwap?.map(ta => (
                    <option key={ta.id} value={ta.id}>{ta.name}</option>
                  ))}
              </select>
            </div>
            <div className={styles.formGroup}>
              <label>Select Task:</label>
              <select
                value={selectedProctoringTask}
                onChange={e => setSelectedProctoringTask(e.target.value)}
                disabled={!selectedTA}
              >
                <option value="">Select Task</option>
                {selectedTA &&
                  proctoringTasks
                    .find(t => t.courseId === selectedTaskId)
                    ?.TASwap?.find(ta => ta.id === selectedTA)
                    ?.availableProctoring.map((t, i) => (
                      <option key={i} value={t.courseId}>{t.course}</option>
                    ))}
              </select>
            </div>
            <div className={styles.formGroup}>
              <label>Message:</label>
              <textarea
                value={message}
                onChange={e => setMessage(e.target.value)}
                placeholder="Write your message here"
                disabled={!selectedProctoringTask}
              />
            </div>
            {errorMessage && (
              <div className={styles.errorMessage}>{errorMessage}</div>
            )}
            <div className={styles.buttonContainer}>
              <button className={styles.approveBtn} onClick={handleSwapRequest}>
                Approve
              </button>
              <button className={styles.cancelBtn} onClick={closeSwap}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Confirmations */}
      {showTransferConfirmation && (
        <ConPop
          message="Submit transfer request?"
          onConfirm={confirmTransfer}
          onCancel={() => setShowTransferConfirmation(false)}
        />
      )}
      {showSwapConfirmation && (
        <ConPop
          message="Submit swap request?"
          onConfirm={confirmSwap}
          onCancel={() => setShowSwapConfirmation(false)}
        />
      )}
    </div>
  );
};

export default TAMonSchPage;
