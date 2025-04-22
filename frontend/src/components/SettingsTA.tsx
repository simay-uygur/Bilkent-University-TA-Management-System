import React, { useState } from 'react';
//import InsNavBar from '../components/InsNavBar';
import styles from './SettingsTA.module.css';

const SettingsTA: React.FC = () => {
  const [editMode, setEditMode] = useState(false);
  const [profile, setProfile] = useState({
    id: '123456',
    password: 'Abcdefgh',
    iban: 'TR',
  });
  const [errors, setErrors] = useState<{ password?: string; iban?: string }>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const toggleEdit = () => {
    setErrors({});
    setEditMode(em => !em);
  };

  const handleSave = () => {
    const newErrors: { password?: string; iban?: string } = {};
    // Password validation
    if (profile.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters.';
    } else if (!/[A-Z]/.test(profile.password)) {
      newErrors.password = 'Password must contain at least one uppercase letter.';
    }
    // IBAN validation
    const cleaned = profile.iban.replace(/\s+/g, '');
    if (!cleaned.startsWith('TR')) {
      newErrors.iban = "IBAN must start with 'TR'";
    } else {
      const digits = cleaned.slice(2);
      if (!/^\d+$/.test(digits)) {
        newErrors.iban = 'IBAN must contain only digits after TR.';
      } else if (digits.length !== 24) {
        newErrors.iban = 'IBAN must be exactly 24 digits after TR.';
      }
    }
    if (Object.keys(newErrors).length) {
      setErrors(newErrors);
      return;
    }
    // All validations passed
    setErrors({});
    setEditMode(false);
    alert('Profile saved!');
  };

  return (
    <div className={styles.pageWrapper}>
        {/* <InsNavBar /> */}
      <main className={styles.content}>
        <h1 className={styles.heading}>Profile Settings</h1>
        <div className={styles.profileContainer}>
          {/* ID (always read-only) */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="id">ID/Username</label>
            <input
              id="id"
              name="id"
              type="text"
              value={profile.id}
              disabled
              className={styles.input}
            />
          </div>
          {/* Password */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              value={profile.password}
              onChange={handleChange}
              disabled={!editMode}
              className={styles.input}
              placeholder={editMode ? '' : '••••••••'}
            />
            {errors.password && <div className={styles.errorText}>{errors.password}</div>}
          </div>
          {/* IBAN */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="iban">IBAN</label>
            <input
              id="iban"
              name="iban"
              type="text"
              value={profile.iban}
              onChange={handleChange}
              disabled={!editMode}
              className={styles.input}

              placeholder="TR"
                maxLength={26}
              
              
            />
            {errors.iban && <div className={styles.errorText}>{errors.iban}</div>}
          </div>
          {/* Buttons */}
          <div className={styles.buttonGroup}>
            {!editMode ? (
              <button onClick={toggleEdit} className={styles.editBtn}>Change</button>
            ) : (
              <button onClick={handleSave} className={styles.saveBtn}>Save</button>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default SettingsTA;