import React from 'react';
import { Home, Bell, Settings, LogOut } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import logo from '../assets/react.svg';
import styles from './NavBarDeans.module.css';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  to?: string;
  onClick?: () => void;
}

const NavBarDeans: React.FC<{ onNotifications: () => void }> = ({ onNotifications }) => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const navItems: NavItem[] = [
    { label: 'Home', icon: <Home size={18} />, to: '/deans-office' },
    { label: 'Notifications', icon: <Bell size={18} />, onClick: onNotifications },
    { label: 'Settings', icon: <Settings size={18} />, to: '/settings' },
    { label: 'Logout', icon: <LogOut size={18} />, to: '/login' },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <img src={logo} alt="Logo" className={styles.logo} />
        <span className={styles.title}>TA Management – Dean’s Office</span>
      </div>
      <nav className={styles.navActions}>
        {navItems.map((item, idx) => (
          <button
            key={idx}
            className={`${styles.navButton} ${
              item.to && pathname.startsWith(item.to) ? styles.active : ''
            }`}
            onClick={() => {
              if (item.to) navigate(item.to);
              else if (item.onClick) item.onClick();
            }}
          >
            <span className={styles.icon}>{item.icon}</span>
            {item.label}
          </button>
        ))}
      </nav>
    </header>
  );
};

export default NavBarDeans;
