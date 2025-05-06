// ErrorPopup.tsx
import React from 'react';
import styles from './ErrPopUp.module.css';
import BlueBut from '../Buttons/BlueBut';

export interface ErrorPopupProps {
  /** The text to display in the popup label */
  message: string;
  /** Called when the user clicks the button */
  onConfirm: () => void;
}

const ErrPopUp: React.FC<ErrorPopupProps> = ({ message, onConfirm }) => (
  <div className={styles.overlay}>
    <div className={styles.popup}>
      <div className={styles.label}>
        {message}
      </div>
      <div className={styles.buttonWrapper}>
        <BlueBut text="I understand" onClick={onConfirm} />
      </div>
    </div>
  </div>
);

export default ErrPopUp;
