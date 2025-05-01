// src/components/NavBarDepartment.tsx
import React from 'react';
import { Bell, Home, LogOut, Settings, ClipboardList, Calendar } from 'lucide-react';
import logo from '../assets/react.svg';
import { useNavigate, useLocation } from 'react-router-dom';
import styles from './NavBarDepartment.module.css';

interface Props {
  onNotifications: () => void;
}

const NavBarDepartment: React.FC<Props> = ({ onNotifications }) => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const navItems = [
    { label: 'Proctor Assignments', icon: <ClipboardList size={18}/>, path: '/dept-office/proctor',   onClick: () => navigate('/dept-office/proctor') },
    { label: 'Pending Leaves',      icon: <Calendar size={18}/>,     path: '/dept-office/leave',     onClick: () => navigate('/dept-office/leave') },
    { label: 'Notifications',       icon: <Bell size={18} />,        path: '',                        onClick: onNotifications },
    {
             label: 'Logout',
              icon: <LogOut size={18} />,
          onClick: () => {
                    navigate('/login', { replace: true });
                      localStorage.removeItem('jwt');
              }
            },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img src={logo} alt="Logo" className={styles.logo}/>
        <span className={styles.title}>TA Management – Department Office</span>
      </div>
      <nav className={styles.navActions}>
        {navItems.map(item => (
          <button
            key={item.label}
            onClick={item.onClick}
            className={`${styles.navButton} ${item.path === pathname ? styles.active : ''}`}
          >
            <span className={styles.icon}>{item.icon}</span>
            {item.label}
          </button>
        ))}
      </nav>
    </header>
  );
};

export default NavBarDepartment;
