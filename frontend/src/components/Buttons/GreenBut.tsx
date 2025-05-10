import React from 'react';
import styles from './GreenBut.module.css';

// Update the props interface to include the disabled property
export interface GreenButtonProps {
  text: string;
  onClick: () => void;
  disabled?: boolean; // Make it optional with a default value
}

const GreenBut: React.FC<GreenButtonProps> = ({ text, onClick, disabled = false }) => {
  return (
    <button 
      className={`${styles.button} ${disabled ? styles.disabled : ''}`}
      onClick={onClick}
      disabled={disabled}
    >
      {text}
    </button>
  );
};

export default GreenBut;


/* GreenButton.tsx */
/* import React from 'react';
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
 */
