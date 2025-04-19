import React from 'react';
import { Notification } from '../api';
import styles from './NotificationPanel.module.css';

interface Props {
  notifications: Notification[];
  onClose: () => void;
  onMarkAllRead: () => void;
}

const NotificationPanel: React.FC<Props> = ({
  notifications,
  onClose,
  onMarkAllRead
}) => (
  <aside className={styles.panel}>
    <header className={styles.header}>
      <h2>Notifications</h2>
      <button className={styles.closeBtn} onClick={onClose}>×</button>
    </header>
    <ul className={styles.list}>
      {notifications.map(n => (
        <li key={n.id} className={styles.item}>
          <div className={styles.content}>
            <strong>{n.source}</strong> – {n.message}
          </div>
          <div className={styles.time}>
            {new Date(n.timestamp).toLocaleString()}
          </div>
        </li>
      ))}
    </ul>
    <footer className={styles.footer}>
      <button onClick={onMarkAllRead}>Mark All as Read</button>
      <button onClick={() => {/* optionally navigate to a full page */}}>
        See All Notifications
      </button>
    </footer>
  </aside>
);

export default NotificationPanel;
