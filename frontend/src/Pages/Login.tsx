import React, { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import NavBar from '../components/NavBar';
import styles from './Login.module.css';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<{ username?: string; password?: string }>({});
  const navigate = useNavigate();

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const newErrors: { username?: string; password?: string } = {};
    // ID: numeric only and exactly 6 digits
    if (/\D/.test(username)) {
      newErrors.username = 'ID must contain only numeric digits.';
    } else if (username.length !== 6) {
      newErrors.username = 'ID must be exactly 6 digits.';
    }
    // Password: ≥8 chars & at least one uppercase
    if (password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters.';
    } else if (!/[A-Z]/.test(password)) {
      newErrors.password = 'Password must include at least one uppercase letter.';
    }

    if (Object.keys(newErrors).length) {
      setErrors(newErrors);
      return;
    }

    setErrors({});
    navigate('/ta');
  };

  return (
    <div className={styles.pageWrapper}>
      <NavBar />

      <div className={styles.container}>
        <div className={styles.card}>
          <h1 className={styles.title}>Sign In</h1>
          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.formGroup}>
              <label htmlFor="username" className={styles.label}>ID/Username</label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                className={styles.input}
              />
              {errors.username && <div className={styles.errorText}>{errors.username}</div>}
            </div>
            <div className={styles.formGroup}>
              <label htmlFor="password" className={styles.label}>Password</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                className={styles.input}
              />
              {errors.password && <div className={styles.errorText}>{errors.password}</div>}
            </div>
            <button type="submit" className={styles.button}>Log In</button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;