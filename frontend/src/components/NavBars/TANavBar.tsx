// src/components/NavBars/TANavBar.tsx
import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Bell, Home, FileText, Settings, LogOut } from 'lucide-react';
import logo from '../../assets/BilkentÜniversitesi-logo.png';
import styles from './TANavBar.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  path: string;
}

const TANavBar: React.FC = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const navItems: NavItem[] = [
    { label: 'Home',             icon: <Home size={18} />,      path: '/ta' },
    { label: 'Make Leave Request', icon: <FileText size={18} />, path: '/ta/leave-request' },
    { label: 'Notifications',    icon: <Bell size={18} />,      path: '/ta/notification' },
    { label: 'Settings',         icon: <Settings size={18} />,  path: '/ta/settings' },
    { label: 'Logout',           icon: <LogOut size={18} />,    path: '/logout' },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img
          src={logo}
          alt="Bilkent University Logo"
          className={styles.logo}
        />
        <span className={styles.title}>
          TA Management - TA
        </span>
      </div>
      <nav className={styles.navActions}>
        {navItems.map(item => {
          const isActive = pathname === item.path;
          return (
            <button
              key={item.path}
              onClick={() => {
                if (item.path === '/logout') {
                  localStorage.removeItem('jwt');
                  navigate('/login', { replace: true });
                } else {
                  navigate(item.path);
                }
              }}
              className={`
                ${styles.navButton}
                ${isActive ? styles.active : ''}
              `}
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

export default TANavBar;
