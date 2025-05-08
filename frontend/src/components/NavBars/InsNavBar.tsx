// src/components/InsNavBar.tsx
import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Bell, Home, LogOut, Settings } from 'lucide-react';
import logo from '../../assets/BilkentÜniversitesi-logo.png';
import styles from './InsNavBar.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  path: string;
}

const InsNavBar: React.FC = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const navItems: NavItem[] = [
    { label: 'Home',           icon: <Home size={18} />,     path: '/instructor' },
    { label: 'Notifications',  icon: <Bell size={18} />,     path: '/instructor/notification' },
    { label: 'Settings',       icon: <Settings size={18} />, path: '/instructor/settings' },
    { label: 'Logout',         icon: <LogOut size={18} />,   path: '/logout' },
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
          TA Management - Instructor
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
                  localStorage.removeItem('userRole');
                  localStorage.removeItem('userId');
                  localStorage.removeItem('userName');
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

export default InsNavBar;
