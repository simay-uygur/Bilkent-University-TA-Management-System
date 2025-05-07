// src/pages/Login.tsx
import React, { useState, FormEvent, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import NavBar from '../../components/NavBars/NavBar';
import { login } from '../../api';
import styles from './Login.module.css';

interface Credentials {
  id: string;
  password: string;
}

interface JwtResponse {
  token: string;
  role: string;
  // other fields as returned
}

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors]   = useState<{ username?: string; password?: string }>({});
  const navigate = useNavigate();
  const location = useLocation();
  const [referrer, setReferrer] = useState<string | null>(null);

  // capture optional ?ref= redirect target
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const r = params.get('ref');
    if (r) setReferrer(r);
  }, [location.search]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const newErrors: { username?: string; password?: string } = {};

    if (!username.trim()) newErrors.username = 'Username is required.';
    if (!password)      newErrors.password = 'Password is required.';

    if (Object.keys(newErrors).length) {
      setErrors(newErrors);
      return;
    }

    try {
      setErrors({});
      const res = await login({ id: username, password });
      const jwt  = res.data?.token;
      const role = res.data?.role;

      if (!jwt) {
        setErrors({ password: 'Invalid username or password.' });
        return;
      }

      // store token
      localStorage.setItem('jwt', jwt);
      // set axios default header if used elsewhere
      // axios.defaults.headers.common['Authorization'] = `Bearer ${jwt}`;

      // choose landing page by role
      let home = '/login';
      switch (role) {
        case 'ROLE_TA':
          home = '/ta';
          break;
        case 'ROLE_INSTRUCTOR':
          home = '/instructor';
          break;
        case 'ROLE_DEPARTMENT_STAFF':
          home = '/department-office';
          break;
        case 'ROLE_DEANS_OFFICE':
          home = '/deans-office';
          break;
          case 'ROLE_ADMIN':
          home = '/admin';
          break;
      }

      navigate(referrer || home, { replace: true });
    } catch (err) {
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
