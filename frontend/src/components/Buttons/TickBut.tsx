// src/components/TickBut.tsx
import React from 'react';
import { Check } from 'lucide-react';
import styles from './TickBut.module.css';

interface TickButProps {
  onApprove: () => void;
}

const TickBut: React.FC<TickButProps> = ({ onApprove }) => (
  <button
    className={`${styles.button} ${styles.approve}`}
    type="button"
    onClick={onApprove}
    aria-label="Approve"
  >
    <Check />
  </button>
);

export default TickBut;