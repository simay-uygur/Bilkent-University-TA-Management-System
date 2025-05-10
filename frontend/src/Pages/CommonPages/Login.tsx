/* src/pages/Login.tsx */
import React, { useState, FormEvent, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import NavBar from '../../components/NavBars/NavBar';
import { login } from '../../api';
import styles from './Login.module.css';
import LoadingPage from './LoadingPage';

interface Credentials {
  id: string;
  password: string;
}

interface JwtResponse {
  token: string;
  role: string;
  userId?: string;
  name?: string;
}

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors]   = useState<{ username?: string; password?: string }>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

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
    setSubmitError(null);

    const newErrors: { username?: string; password?: string } = {};
    if (!username.trim()) newErrors.username = 'Username is required.';
    if (!password)      newErrors.password = 'Password is required.';
    if (Object.keys(newErrors).length) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      const res = await login({ id: username, password });
      const data: JwtResponse = res.data;
      const jwt = data.token;
      const role = data.role;
      const userId = data.userId || username;
      const name = data.name;

      if (!jwt) {
        setErrors({ password: 'Invalid username or password.' });
        setLoading(false);
        return;
      }

      localStorage.setItem('jwt', jwt);
      localStorage.setItem('userId', userId);
      localStorage.setItem('userRole', role);
      if (name) localStorage.setItem('userName', name);

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
      }

      navigate(referrer || home, { replace: true });
    } catch (err) {
      console.error('Login failed', err);
      setSubmitError('An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) return <LoadingPage />;

  return (
    <div className={styles.loginPageWrapper}>
      <NavBar />

      <div className={styles.container}>
        <div className={styles.card}>
          <h1 className={styles.title}>Sign In</h1>
          {submitError && <div className={styles.errorText}>{submitError}</div>}
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
            <p className={styles.forgotLink}>
              <Link to="/forgot-password">Forgot my password?</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;