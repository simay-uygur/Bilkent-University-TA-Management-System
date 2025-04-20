// src/components/TANavBar.tsx
import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { Bell, Home, Calendar, FileText, LogOut } from 'lucide-react';
import logo from '../assets/react.svg';
import styles from './TANavBar.module.css';

interface TANavBarProps {
  onNotificationsClick: () => void;
}

const TANavBar: React.FC<TANavBarProps> = ({ onNotificationsClick }) => {
  const navigate = useNavigate();

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img
          src={logo}
          alt="Bilkent University Logo"
          className={styles.logo}
        />
        <span className={styles.title}>TA Management – TA</span>
      </div>

      <nav className={styles.navActions}>
        <NavLink
          to="/dashboard"
          className={({ isActive }) =>
            `${styles.navButton} ${isActive ? styles.active : ''}`
          }
        >
          <Home size={18} className={styles.icon} />
          Home
        </NavLink>

        <NavLink
          to="/volunteer"
          className={({ isActive }) =>
            `${styles.navButton} ${isActive ? styles.active : ''}`
          }
        >
          <Calendar size={18} className={styles.icon} />
          Volunteer Proctoring
        </NavLink>

        <NavLink
          to="/leave-request/1"
          className={({ isActive }) =>
            `${styles.navButton} ${isActive ? styles.active : ''}`
          }
        >
          <FileText size={18} className={styles.icon} />
          Make Leave Request
        </NavLink>

        <button
          className={styles.navButton}
          onClick={onNotificationsClick}
        >
          <Bell size={18} className={styles.icon} />
          Notifications
        </button>

        <button
          className={`${styles.navButton} ${styles.logoutButton}`}
          onClick={() => {
            // TODO: clear auth state if needed
            navigate('/login', { replace: true });
          }}
        >
          <LogOut size={18} className={styles.icon} />
          Logout
        </button>
      </nav>
    </header>
  );
};

export default TANavBar;
