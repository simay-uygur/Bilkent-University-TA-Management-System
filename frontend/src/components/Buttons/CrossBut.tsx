import React from 'react';
import { X } from 'lucide-react';
import styles from './CrossBut.module.css';

interface CrossButProps {
  onReject: () => void;
}

const CrossBut: React.FC<CrossButProps> = ({ onReject }) => (
  <button
    className={`${styles.CrossBut} ${styles.reject}`}
    type="button"
    onClick={onReject}
    aria-label="Reject"
  >
    <X />
  </button>
);

export default CrossBut;