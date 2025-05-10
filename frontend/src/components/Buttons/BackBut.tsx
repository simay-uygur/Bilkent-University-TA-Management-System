// src/components/Buttons/BackBut.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import styles from './BackBut.module.css';

interface BackButtonProps {
  to?: string;
  onClick?: () => void;
}

const BackButton: React.FC<BackButtonProps> = ({ to, onClick }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    if (onClick) {
      onClick();
    } else if (to) {
      navigate(to);
    } else {
      navigate(-1);
    }
  };

  return (
    <button
      type="button"
      className={styles.backButton}
      onClick={handleClick}
      aria-label="Go back"
    >
      <ArrowLeft size={30} />
    </button>
  );
};

export default BackButton;
