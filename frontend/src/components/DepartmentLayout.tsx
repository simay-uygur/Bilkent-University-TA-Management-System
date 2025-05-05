// src/components/DepartmentLayout.tsx
import React, { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import NavBarDepartment from './NavBarDepartment';
import NotificationPanel from './NotificationPanel';
import SearchSelect from './SearchSelect';
import { fetchNotifications, markAllRead, Notification } from '../api'; 
import styles from './DepartmentLayout.module.css';
import MultiSearch from './MultiSearch';

export default function DepartmentLayout() {
  const [showNotif, setShowNotif] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    if (showNotif) {
      fetchNotifications()
        .then(r => setNotifications(r.data))
        .catch(console.error);
    }
  }, [showNotif]);

  const handleMarkAllRead = () => {
    markAllRead()
      .then(() => setNotifications([]))
      .catch(console.error);
  };

  return (
    <div className={styles.wrapper}>
      <NavBarDepartment onNotifications={() => setShowNotif(true)} />
      <div className={styles.searchContainer}>
        <MultiSearch />
      </div>
      {showNotif && (
        <NotificationPanel
          notifications={notifications}
          onClose={() => setShowNotif(false)}
          onMarkAllRead={handleMarkAllRead}
        />
      )}
  
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}
