/* src/pages/AssignTA/FacultyFilter.tsx */
import React from 'react';
import styles from './FacultyFilter.module.css';

interface FacultyFilterProps {
  faculties: string[];
  selected: string;
  onChange: (faculty: string) => void;
}

const FacultyFilter: React.FC<FacultyFilterProps> = ({ faculties, selected, onChange }) => {
  return (
    <select
      className={styles.dropdown}
      value={selected}
      onChange={e => onChange(e.target.value)}
    >
      {faculties.map(f => (
        <option key={f} value={f}>{f}</option>
      ))}
    </select>
  );
};

export default FacultyFilter;
