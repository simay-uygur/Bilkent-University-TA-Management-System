import React from 'react';
import logo from '../assets/react.svg';
import styles from './NavBar.module.css';

const NavBar: React.FC = () => (
  <header className={styles.header}>
    <div className={styles.logoSection}>
      <img src={logo} alt="Bilkent University Logo" className={styles.logo} />
      <span className={styles.title}>TA Management - TA</span>
    </div>
  </header>
);

export default NavBar;