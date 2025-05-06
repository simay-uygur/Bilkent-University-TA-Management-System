import React, { useState, FormEvent, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import NavBar from '../../components/NavBars/NavBar';
import styles from './Login.module.css';

interface LoginErrors {
  username?: string;
  password?: string;
}

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors]     = useState<LoginErrors>({});
  const [referrer, setReferrer] = useState<string | null>(null);
  const navigate                = useNavigate();
  const location                = useLocation();

  // Parse optional ?ref=/some/path
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const r = params.get('ref');
    if (r) setReferrer(r);
  }, [location.search]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    // Client-side validation
    const newErrors: LoginErrors = {};
    if (!username.trim()) newErrors.username = 'Username is required.';
    if (!password.trim()) newErrors.password = 'Password is required.';
    if (Object.keys(newErrors).length) {
      setErrors(newErrors);
      return;
    }
    setErrors({});

    try {
      axios.defaults.baseURL = 'http://localhost:5173';
      const resp = await axios.post('/api/auth/login', { username, password });

      if (resp.status === 200 && resp.data.token) {
        const token = resp.data.token;
        // Fetch user role
        const roleResp = await axios.post<string>('/api/auth/user-role', { username, password });
        const role     = roleResp.data;

        // Persist in localStorage
        localStorage.setItem('userToken', token);
        localStorage.setItem('username', username);
        localStorage.setItem('role', role);

        toast.success('Login successful! Redirecting…');

        setTimeout(() => {
          if (referrer) {
            navigate(referrer, { replace: true });
          } else {
            switch (role) {
              case 'TA':
                navigate('/coordinator-homepage');
                break;
              case 'DepartmentOffice':
                navigate('/counselor-homepage');
                break;
              case 'DeanOffice':
                navigate('/tourguide-homepage');
                break;
              case 'Instructor':
                navigate('/advisor-homepage');
                break;
              case 'Admin':
                navigate('/admin-dashboard');
                break;
              default:
                toast.error('Unknown role. Please contact support.');
                navigate('/login', { replace: true });
            }
          }
        }, 800);
      } else {
        toast.error('Login failed. Please check your credentials.');
      }
    } catch (err: any) {
      console.error(err);
      if (err.response?.status === 401) {
        toast.error('Invalid username or password.');
      } else {
        toast.error('Server error. Please try again later.');
      }
    }
  };

  return (
    <div className={styles.loginPageWrapper}>
      <NavBar />
      <ToastContainer position="top-right" autoClose={3000} />

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
                placeholder="Enter your username"
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
                placeholder="Enter your password"
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
