import React, { useState } from 'react';
import InsNavBar from '../../components/NavBars/InsNavBar';
import styles from './Settings.module.css';

const Settings: React.FC = () => {
  const [editMode, setEditMode] = useState(false);
  const [profile, setProfile] = useState({
    id: '123456',
    password: 'Abcdefgh',
  });
  const [errors, setErrors] = useState<{ password?: string }>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const toggleEdit = () => {
    setErrors({});
    setEditMode(em => !em);
  };

  const handleSave = () => {
    const newErrors: { password?: string } = {};
    // Password validation
    if (profile.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters.';
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
      <InsNavBar />
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

export default Settings;
