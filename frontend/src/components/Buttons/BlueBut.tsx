import React from 'react';
import styles from './BlueBut.module.css';

interface BlueButtonProps {
  text: string;
  onClick?: () => void;
}

const BlueBut: React.FC<BlueButtonProps> = ({ text, onClick }) => (
  <button className={styles.blueButton} onClick={onClick}>
    {text}
  </button>
);

export default BlueBut;

