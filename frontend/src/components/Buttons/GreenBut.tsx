/* GreenButton.tsx */
import React from 'react';
import styles from './GreenBut.module.css';

interface GreenButtonProps {
  text: string;
  onClick?: () => void;
}

const GreenBut: React.FC<GreenButtonProps> = ({ text, onClick }) => (
  <button className={styles.greenButton} onClick={onClick}>
    {text}
  </button>
);

export default GreenBut;

