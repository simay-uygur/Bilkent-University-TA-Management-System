// src/pages/ViewLogs.tsx
import React, { useEffect, useState } from 'react';
import AdminNavBar from '../../components/NavBars/AdminNavBar';
import AdminLog from '../../components/AdminLog';
import styles from './ViewLogs.module.css';

interface LogEntry {
  id: string;
  message: string;
  dateStr: string; // e.g. "25.04.2023"
  timeStr: string; // e.g. "16:45"
  variantClass?: string;
}

const ViewLogs: React.FC = () => {
  const [logs, setLogs] = useState<LogEntry[]>([]);

  useEffect(() => {
    // TODO: fetch logs from backend
    setLogs([
      { id: '1', message: 'User Alice created.', dateStr: '25.04.2023', timeStr: '16:45'},
      { id: '2', message: 'Classroom 101 updated.', dateStr: '25.04.2023', timeStr: '16:43'},
      // …more entries
    ]);
  }, []);

  const handleDelete = (id: string) => {
    setLogs(prev => prev.filter(log => log.id !== id));
  };


  // Sort logs by date/time descending
  const sortedLogs = [...logs].sort((a, b) => {
    const [da, ma, ya] = a.dateStr.split('.').map(Number);
    const [db, mb, yb] = b.dateStr.split('.').map(Number);
    const [ha, mna] = a.timeStr.split(':').map(Number);
    const [hb, mnb] = b.timeStr.split(':').map(Number);
    const dateA = new Date(ya, ma - 1, da, ha, mna);
    const dateB = new Date(yb, mb - 1, db, hb, mnb);
    return dateB.getTime() - dateA.getTime();
  });

  return (
    <div className={styles.container}>
      <AdminNavBar />
      <h1 className={styles.header}>View Logs</h1>
      <div className={styles.logsList}>
        {sortedLogs.map(({ id, message, dateStr, timeStr, variantClass }) => (
          <AdminLog
            key={id}
            label={message}
            visible
            dateStr={dateStr}
            timeStr={timeStr}
            variantClass={variantClass}
            onDelete={() => handleDelete(id)}
          />
        ))}
      </div>
    </div>
  );
};

export default ViewLogs;