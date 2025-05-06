// src/components/NavBars/TANavBar.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Home, Calendar, FileText, LogOut, Settings } from 'lucide-react';
import logo from '../../assets/BilkentÜniversitesi-logo.png';
import styles from './TANavBar.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
  active?: boolean;
}

const TANavBar: React.FC = () => {
  const navigate = useNavigate();

  const navItems: NavItem[] = [
    {
      label: 'Home',
      icon: <Home size={18} />,
      onClick: () => navigate('/ta'),
      active: true,
    },
    {
      label: 'Make Leave Request',
      icon: <FileText size={18} />,
      onClick: () => navigate('/make'), // adjust to your route
    },
    {
      label: 'Notifications',
      icon: <Bell size={18} />,
      onClick: () => navigate('/not'),
    },
    {
      label: 'Settings',
      icon: <Settings size={18} />,
      onClick: () => navigate('/set'),
    },
    {
          label: 'Logout',
          icon: <LogOut size={18} />,
          onClick: () => {
            localStorage.removeItem('jwt');
            navigate('/login', { replace: true });
          }
        },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img src={logo} alt="Bilkent University Logo" className={styles.logo} />
        <span className={styles.title}>TA Management - TA</span>
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
};

export default TANavBar;
