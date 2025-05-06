import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './RequestTAForm.module.css';

const roles = ['Grading', 'Proctoring', 'Lab TA'] as const;

export default function RequestTAForm() {
  const { courseId } = useParams<{ courseId: string }>();
  const navigate     = useNavigate();

  const [numTAs, setNumTAs]           = useState(1);
  const [selectedRoles, setRoles]     = useState<string[]>([]);
  const [preferred, setPreferred]     = useState<string>('');
  const [nonPreferred, setNonPreferred] = useState<string>('');

  const toggleRole = (role: string) => {
    setRoles(r =>
      r.includes(role) ? r.filter(x => x !== role) : [...r, role]
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: send to API
    console.log({
      courseId,
      numTAs,
      selectedRoles,
      preferred,
      nonPreferred
    });
    navigate(-1);
  };

  return (
    <div className={styles.pageWrapper}>
      <form className={styles.card} onSubmit={handleSubmit}>
        <h1 className={styles.heading}>Define TA Needs</h1>

        <label className={styles.label}>Select Course:</label>
        <input
          type="text"
          value={courseId}
          disabled
          className={styles.input}
        />

        <label className={styles.label}>Number of TAs Required:</label>
        <input
          type="number"
          min={1}
          value={numTAs}
          onChange={e => setNumTAs(Number(e.target.value))}
          className={styles.input}
        />

        <fieldset className={styles.fieldset}>
          <legend className={styles.label}>TA Role Selection:</legend>
          {roles.map(role => (
            <label key={role} className={styles.checkboxLabel}>
              <input
                type="checkbox"
                checked={selectedRoles.includes(role)}
                onChange={() => toggleRole(role)}
              />
              {role}
            </label>
          ))}
        </fieldset>

        <label className={styles.label}>Preferred TA(s):</label>
        <input
          type="text"
          placeholder="Search preferred TA…"
          value={preferred}
          onChange={e => setPreferred(e.target.value)}
          className={styles.input}
        />

        <label className={styles.label}>Non‑Preferred TA(s):</label>
        <input
          type="text"
          placeholder="Search non‑preferred TA…"
          value={nonPreferred}
          onChange={e => setNonPreferred(e.target.value)}
          className={styles.input}
        />

        <div className={styles.actions}>
          <button type="submit" className={styles.submitBtn}>
            Save and Send
          </button>
        </div>
      </form>
    </div>
  );
}
