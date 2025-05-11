import React, { useState, FormEvent } from 'react';
import { useNavigate , Link} from 'react-router-dom';
import NavBar from '../../components/NavBars/NavBar';
import ConPop from '../../components/PopUp/ConPop';
import styles from './ForgotPassword.module.css';

const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [errors, setErrors] = useState<{ email?: string; newPassword?: string }>({});
  const [showConfirm, setShowConfirm] = useState(false);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const errs: { email?: string; newPassword?: string } = {};
    if (!email.trim()) errs.email = 'Email is required.';
    if (!newPassword) errs.newPassword = 'New password is required.';
    setErrors(errs);
    if (Object.keys(errs).length === 0) {
      setShowConfirm(true);
    }
  };

  const onConfirm = () => {
    // TODO: call API to reset password
    setShowConfirm(false);
    navigate('/login');
  };
  const onCancel = () => setShowConfirm(false);

  return (
    <div className={styles.loginPageWrapper}>
      <NavBar />
      <div className={styles.container}>
        <div className={styles.card}>
          <h1 className={styles.title}>Reset Password</h1>
          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.formGroup}>
              <label htmlFor="email" className={styles.label}>Email</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                className={styles.input}
              />
              {errors.email && <div className={styles.errorText}>{errors.email}</div>}
            </div>
            <div className={styles.formGroup}>
              <label htmlFor="newPassword" className={styles.label}>New Password</label>
              <input
                id="newPassword"
                type="password"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                className={styles.input}
              />
              {errors.newPassword && <div className={styles.errorText}>{errors.newPassword}</div>}
            </div>
            <button type="submit" className={styles.button}>Submit</button>
            <p className={styles.loginLink}>
              <Link to="/login">Login</Link>
            </p>
          </form>
        </div>
      </div>

      {showConfirm && (
        <ConPop
          message="Are you sure you want to reset your password?"
          onConfirm={onConfirm}
          onCancel={onCancel}
        />
      )}
    </div>
  );
};

export default ForgotPassword;