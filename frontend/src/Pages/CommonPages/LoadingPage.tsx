// src/components/LoadingPage.tsx
import React from 'react';
import styles from './LoadingPage.module.css';

const LoadingPage: React.FC = () => (
  <div className={styles.loaderContainer}>
    <div className={styles.loader}></div>
  </div>
);

export default LoadingPage;
