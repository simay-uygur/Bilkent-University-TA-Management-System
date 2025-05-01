import React, { useState, useEffect } from 'react';
import NotificationPanel from '../components/NotificationPanel';
import { fetchNotifications, Notification } from '../api';
import styles from './Notifications.module.css';

export default function Notifications() {
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);

  // load data whenever the panel opens
  useEffect(() => {
    if (!open) return;
    fetchNotifications()
      .then(res => setNotifications(res.data))
      .catch(err => console.error('Failed to load notifications', err));
  }, [open]);

  return (
    <div className={styles.container}>
      <button
        className={styles.toggleBtn}
        onClick={() => setOpen(o => !o)}
      >
        {open ? 'Hide Notifications' : 'Show Notifications'}
      </button>

      {open && (
        <NotificationPanel
          notifications={notifications}
          onClose={() => setOpen(false)} onMarkAllRead={function (): void {
            throw new Error('Function not implemented.');
          } }        />
      )}
    </div>
  );
}