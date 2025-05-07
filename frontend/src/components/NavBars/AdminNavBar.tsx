// src/components/DeanOfNavBar.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Home, LogOut, Settings, Eye } from 'lucide-react';
import logo from '../../assets/BilkentÜniversitesi-logo.png';
import styles from './DeanOfNavBar.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
  active?: boolean;
}

const DeanOfNavBar: React.FC = () => {
  const navigate = useNavigate();

  const navItems: NavItem[] = [
    {
      label: 'Home',
      icon: <Home size={18} />,
      onClick: () => navigate('/admin'),
      active: true,
    },
    {
      label: 'View Project Structure',
      icon: <Eye size={18} />,
      onClick: () => {}, // placeholder, no navigation
    },
    {
      label: 'Logs',
      icon: <Bell size={18} />,
      onClick: () => navigate('/admin/view-logs'),
    },
    {
      label: 'Settings',
      icon: <Settings size={18} />,
      onClick: () => navigate('/admin/settings'),
    },
    {
      label: 'Logout',
      icon: <LogOut size={18} />,
      onClick: () => {
        // TODO: perform logout logic
        navigate('/login');
      },
    },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img src={logo} alt="Bilkent University Logo" className={styles.logo} />
        <span className={styles.title}>TA Management - Admin</span>
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

export default DeanOfNavBar;
