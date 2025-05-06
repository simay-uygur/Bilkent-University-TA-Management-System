// src/components/TANavBar.tsx
import React from 'react';
import { Bell, Home, LogOut, Settings } from 'lucide-react';
import logo from '../../assets/BilkentÜniversitesi-logo.png';
import styles from './DepOfNavBar.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  onClick?: () => void;
  active?: boolean;
}

// For Dean's Office: only Home, Notifications, Logout
const navItems: NavItem[] = [
  { label: 'Home', icon: <Home size={18} />, active: true },
  { label: 'Notifications', icon: <Bell size={18} /> },
  { label: 'Settings', icon: <Settings size={18} /> },
  { label: 'Logout', icon: <LogOut size={18} /> },
];

const DepOfNavBar: React.FC = () => (
  <header className={styles.header}>
    <div className={styles.logoSection}>
      <img src={logo} alt="Bilkent University Logo" className={styles.logo} />
      <span className={styles.title}>TA Management - Department Office</span>
    </div>
    <nav className={styles.navActions}>
      {navItems.map((item, idx) => (
        <button
          key={idx}
          onClick={item.onClick}
          className={`${styles.navButton} ${item.active ? styles.active : ''}`}
        >
          <span className={styles.icon}>{item.icon}</span>
          {item.label}
        </button>
      ))}
    </nav>
  </header>
);

export default DepOfNavBar;