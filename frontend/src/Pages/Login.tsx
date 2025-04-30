// src/pages/Login.tsx
import React, { useState, FormEvent, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import NavBar from '../components/NavBar';
import { login } from '../api';
import styles from './Login.module.css';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors]   = useState<{ username?: string; password?: string }>({});
  const navigate = useNavigate();
  const location = useLocation();
  const [referrer, setReferrer] = useState<string | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const r = params.get('ref');
    if (r) setReferrer(r);
  }, [location.search]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const newErrors: { username?: string; password?: string } = {};

    if (!username) newErrors.username = 'Username is required.';
    if (!password) newErrors.password = 'Password is required.';

    if (Object.keys(newErrors).length) {
      setErrors(newErrors);
      return;
    }

    try {
            setErrors({});
            const res = await login({ id: username, password });
            const jwt = res.data?.token;
            // if no token returned, treat as invalid credentials
            if (!jwt) {
              setErrors({ password: 'Invalid username or password.' });
              return;
            }
            // store JWT
            localStorage.setItem('jwt', jwt);
            // redirect
            navigate(referrer || '/dashboard', { replace: true });
          } catch (err: any) {
            console.error('Login failed', err);
            setErrors({ password: 'Invalid username or password.' });
          }
  };

  return (
    <div className={styles.loginPageWrapper}>
      <NavBar />

      <div className={styles.container}>
        <div className={styles.card}>
          <h1 className={styles.title}>Sign In</h1>
          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.formGroup}>
              <label htmlFor="username" className={styles.label}>Username</label>
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
