// src/components/InsNavBar.tsx
import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { Bell, Home, Settings, LogOut } from 'lucide-react';
import logo from '../assets/react.svg';
import styles from './InsNavBar.module.css';

const InsNavBar: React.FC = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    // TODO: clear any auth tokens / context here
    navigate('/login', { replace: true });
  };

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img src={logo} alt="Bilkent University Logo" className={styles.logo} />
        <span className={styles.title}>TA Management – Instructor</span>
      </div>

      <nav className={styles.navActions}>
        <NavLink
          to="/instructor"
          className={({ isActive }) =>
            `${styles.navButton} ${isActive ? styles.active : ''}`
          }
        >
          <Home size={18} className={styles.icon} />
          Home
        </NavLink>

        <button
          className={styles.navButton}
          onClick={() => navigate('/instructor/workload')}
        >
          <Bell size={18} className={styles.icon} />
          Check Workload
        </button>

        <NavLink
          to="/instructor/settings"
          className={({ isActive }) =>
            `${styles.navButton} ${isActive ? styles.active : ''}`
          }
        >
          <Settings size={18} className={styles.icon} />
          Settings
        </NavLink>

        <button
          className={`${styles.navButton} ${styles.logoutButton}`}
          onClick={handleLogout}
        >
          <LogOut size={18} className={styles.icon} />
          Logout
        </button>
      </nav>
    </header>
  );
};

export default InsNavBar;
