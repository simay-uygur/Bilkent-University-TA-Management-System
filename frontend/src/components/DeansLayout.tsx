import React, { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import NavBarDeans from './NavBarDeans';
import NotificationPanel from './NotificationPanel';
import { fetchNotifications, markAllRead } from '../api';
import styles from './DeansLayout.module.css';

const DeanLayout: React.FC = () => {
  const [showNotif, setShowNotif] = useState(false);
  const [notifications, setNotifications] = useState<any[]>([]);

  useEffect(() => {
    if (showNotif) {
      fetchNotifications().then(r => setNotifications(r.data));
    }
  }, [showNotif]);

  const handleMarkAll = () =>
    markAllRead().then(() => setNotifications([]));

  return (
    <div className={styles.wrapper}>
      <NavBarDeans onNotifications={() => setShowNotif(true)} />

      {showNotif && (
        <NotificationPanel
          notifications={notifications}
          onClose={() => setShowNotif(false)}
          onMarkAllRead={handleMarkAll}
        />
      )}

      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
};

export default DeanLayout;
