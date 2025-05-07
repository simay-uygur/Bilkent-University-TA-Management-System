// src/components/TANavBar.tsx
import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Home,
  BookOpen,
  UserPlus,
  UserCheck,
  Bell,
  Settings,
  LogOut
} from 'lucide-react';
import logo from '../../assets/BilkentÜniversitesi-logo.png';
import styles from './DepOfNavBar.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  path: string;
}

const DepOfNavBar: React.FC = () => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const navItems: NavItem[] = [
    { label: 'Home',            icon: <Home size={18} />,            path: '/department-office' },
    { label: 'CourseTA', icon: <UserPlus size={18} />,        path: '/department-office/assign-course' },
    { label: 'Proctoring',  icon: <UserCheck size={18} />,       path: '/department-office/assign-proctor' },
    { label: 'Notifications',   icon: <Bell size={18} />,            path: '/department-office/notification' },
    { label: 'Settings',        icon: <Settings size={18} />,        path: '/department-office/settings' },
    { label: 'Logout',          icon: <LogOut size={18} />,          path: '/login' },
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
          TA Management - Department Office
        </span>
      </div>

      <nav className={styles.navActions}>
        {navItems.map(item => {
          const isActive = pathname === item.path;
          const isLogout = item.label === 'Logout';

          return (
            <button
              key={item.label}
              onClick={() => {
                if (isLogout) {
                  localStorage.removeItem('jwt');
                  navigate(item.path, { replace: true });
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

export default DepOfNavBar;
