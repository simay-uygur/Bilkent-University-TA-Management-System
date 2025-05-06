// src/components/InsNavBar.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Home, LogOut, Settings } from 'lucide-react';
import logo from '../../assets/BilkentÜniversitesi-logo.png';
import styles from './InsNavBar.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
  active?: boolean;
}

const InsNavBar: React.FC = () => {
  const navigate = useNavigate();

  const navItems: NavItem[] = [
    {
      label: 'Home',
      icon: <Home size={18} />,
      onClick: () => navigate('/ins'),
      active: true,
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
        // Add your logout logic here, then:
        navigate('/login');
      },
    },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img src={logo} alt="Bilkent University Logo" className={styles.logo} />
        <span className={styles.title}>TA Management - Instructor</span>
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

export default InsNavBar;
