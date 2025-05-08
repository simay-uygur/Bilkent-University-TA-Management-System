// src/components/AdminLog.tsx
import React from 'react';
import { Info, Trash2 } from 'lucide-react';
import styles from './AdminLog.module.css';

interface AdminLogProps {
  label: string;
  visible: boolean;
  dateStr: string;
  timeStr: string;
  onDelete: () => void;
  variantClass?: string;
}

const AdminLog: React.FC<AdminLogProps> = ({
  label,
  visible,
  dateStr,
  timeStr,
  onDelete,
  variantClass = ''
}) => {
  if (!visible) return null;

  return (
    <div className={`${styles.card} ${variantClass}`}> 
      <Info size={20} className={styles.icon} />
      <div className={styles.content}>
        <p className={styles.message}>{label}</p>
      </div>
      <span className={styles.datetime}>{`${dateStr} ${timeStr}`}</span>
      <button className={styles.trashButton} onClick={onDelete} aria-label="Delete log">
        <Trash2 size={18} />
      </button>
    </div>
  );
};

export default AdminLog;