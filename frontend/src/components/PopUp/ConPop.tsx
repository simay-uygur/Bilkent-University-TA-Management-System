import React from 'react';
import styles from './ConPop.module.css';

interface ConfirmationModalProps {
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConPop: React.FC<ConfirmationModalProps> = ({ message, onConfirm, onCancel }) => (
  <div className={styles.overlay}>
    <div className={styles.modal}>
      <p className={styles.message}>{message}</p>
      <div className={styles.buttons}>
        <button className={styles.confirm} onClick={onConfirm}>Yes</button>
        <button className={styles.cancel} onClick={onCancel}>No</button>
      </div>
    </div>
  </div>
);

export default ConPop;