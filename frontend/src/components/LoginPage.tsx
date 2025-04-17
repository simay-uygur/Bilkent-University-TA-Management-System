import React, { useState, FormEvent } from 'react';
import axios from 'axios';
import { login } from '../api';
import styles from './LoginPage.module.css';
import { useNavigate } from 'react-router-dom';

const LoginPage: React.FC = () => {
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [error, setError]       = useState<string>('');
    const navigate = useNavigate();                     // ← new
  
    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      setError('');
  
      // client‑side checks
      if (!username || !password) {
        setError('Username & password are required');
        return;
      }
  
      try {
        await login({ username, password });
        navigate('/dashboard');       // ← use in‑app navigation
      } catch (err) {
        if (axios.isAxiosError(err)) {
          setError(err.response?.data?.message ?? 'Login failed');
        } else {
          setError((err as Error).message);
        }
      }
    };

  return (
    <>
      <header className={styles.header}>
        <div className={styles.headerTitle}>TA Management</div>
      </header>

      <div className={styles.container}>
        <div className={styles.card}>
          <h1 className={styles.title}>Sign In</h1>
          <form className={styles.form} onSubmit={handleSubmit}>
            <div className={styles.formGroup}>
              <label htmlFor="username" className={styles.label}>
                Username
              </label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                required
                className={styles.input}
              />
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="password" className={styles.label}>
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
                className={styles.input}
              />
            </div>

            {error && <p className={styles.error}>{error}</p>}

            <button type="submit" className={styles.button}>
              Log In
            </button>
          </form>
        </div>
      </div>
    </>
  );
};

export default LoginPage;
