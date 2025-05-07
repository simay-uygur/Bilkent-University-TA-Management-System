// src/components/DeanOfNavBar.tsx
import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Home, Bell, Settings, LogOut, BookOpen } from 'lucide-react';
import logo from '../../assets/BilkentÜniversitesi-logo.png';
import styles from './DeanOfNavBar.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  path: string;
  logout?: boolean;
}

const DeanOfNavBar: React.FC = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const navItems: NavItem[] = [
    { label: 'Home',          icon: <Home size={18} />,     path: '/deans-office' },
    { label: 'Proctoring',    icon: <BookOpen size={18} />, path: '/deans-office/proctor' },
    { label: 'Manage Exams',  icon: <BookOpen size={18} />, path: '/deans-office/view-add-exams' },
    { label: 'Notifications', icon: <Bell size={18} />,     path: '/deans-office/notification' },
    { label: 'Settings',      icon: <Settings size={18} />, path: '/deans-office/settings' },
    { label: 'Logout',        icon: <LogOut size={18} />,   path: '/login', logout: true },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img src={logo} alt="Bilkent University Logo" className={styles.logo} />
        <span className={styles.title}>TA Management - Dean Office</span>
      </div>
      <nav className={styles.navActions}>
        {navItems.map(item => {
          let isActive = false;
          if (!item.logout) {
            if (item.path === '/deans-office') {
              // Home: only active on exactly /deans-office
              isActive = pathname === item.path;
            } else {
              // Others: active on path or any deeper sub‑route
              isActive = pathname === item.path || pathname.startsWith(item.path + '/');
            }
          }

          return (
            <button
              key={item.path}
              onClick={() => {
                if (item.logout) {
                  localStorage.removeItem('jwt');
                  navigate(item.path, { replace: true });
                } else {
                  navigate(item.path);
                }
              }}
              className={`${styles.navButton} ${isActive ? styles.active : ''}`}
            >
              <span className={styles.icon}>{item.icon}</span>
              {item.label}
            </button>
          );
        })}
      </nav>
    </header>
  );
};

export default DeanOfNavBar;
