import React from 'react';
import ReactCalendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';
import styles from './Calendar.module.css';

interface CalendarProps {
  date: Date;
  onDateChange: (newDate: Date) => void;
  startTime: string;
  onStartTimeChange: (newTime: string) => void;
}

const Calendar: React.FC<CalendarProps> = ({
  date,
  onDateChange,
  startTime,
  onStartTimeChange
}) => (
  <div className={styles.container}>
    {/* Month‑grid calendar */}
    <ReactCalendar
      onChange={(value, _event) => {
        if (value instanceof Date) {
          onDateChange(value);
        }
      }}
      value={date}
      calendarType="gregory"
      className={styles.calendar}
    />

    {/* Time picker below */}
    <div className={styles.timePicker}>
      <label htmlFor="timePicker" className={styles.label}>
        Start Time
      </label>
      <input
        id="timePicker"
        type="time"
        className={styles.input}
        value={startTime}
        onChange={e => onStartTimeChange(e.target.value)}
      />
    </div>
  </div>
);

export default Calendar;
