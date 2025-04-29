// src/components/NavBarDeans.tsx
import React from 'react';
import { Home, Bell, Settings, LogOut } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import logo from '../assets/react.svg';
import styles from './NavBarDeans.module.css';

interface Props {
  onNotifications: () => void;
}

const NavBarDeans: React.FC<Props> = ({ onNotifications }) => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const items = [
    { label: 'Home',          icon: <Home size={18} />,      action: () => navigate('/deans-office'), active: pathname === '/deans-office' },
    { label: 'Notifications', icon: <Bell size={18} />,      action: onNotifications },
    { label: 'Settings',      icon: <Settings size={18} />,  action: () => navigate('/settings') },
    { label: 'Logout',        icon: <LogOut size={18} />,    action: () => navigate('/login', { replace: true }) },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img src={logo} alt="Logo" className={styles.logo} />
        <span className={styles.title}>TA Management – Dean’s Office</span>
      </div>
      <nav className={styles.navActions}>
        {items.map(i => (
          <button
            key={i.label}
            onClick={i.action}
            className={`${styles.navButton} ${i.active ? styles.active : ''}`}
          >
            <span className={styles.icon}>{i.icon}</span>
            {i.label}
          </button>
        ))}
      </nav>
    </header>
  );
};

export default NavBarDeans;
