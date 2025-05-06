import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import styles from './BackBut.module.css';

interface BackButtonProps {
  /** Optional path to go to instead of history.back() */
  to?: string;
}

const BackButton: React.FC<BackButtonProps> = ({ to }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    if (to) navigate(to);
    else navigate(-1);
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