import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { updateSettings } from '../services/api';
import './Settings.css';

export default function Settings() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    if (!token || !user) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      navigate('/login');
      return;
    }
    setUsername(user.username || '');
  }, [navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (password && password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (password) {
      if (password.length < 8) {
        setError('Password must be at least 8 characters');
        return;
      }
      if (!/[A-Z]/.test(password)) {
        setError('Password must include an uppercase letter');
        return;
      }
      if (!/[a-z]/.test(password)) {
        setError('Password must include a lowercase letter');
        return;
      }
      if (!/[0-9]/.test(password)) {
        setError('Password must include a digit');
        return;
      }
    }

    setLoading(true);
    try {
      const body = { username };
      if (password) body.password = password;

      await updateSettings(body);

      // Update local user
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      user.username = username;
      localStorage.setItem('user', JSON.stringify(user));

      setSuccess('Settings updated successfully!');
      setPassword('');
      setConfirmPassword('');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to update settings');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="settings-page" id="settings-page">
      <Navbar />
      <div className="settings-container">
        <div className="settings-card">
          <div className="settings-header">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="3"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
            <h1>Settings</h1>
            <p>Update your profile information</p>
          </div>

          {error && (
            <div className="settings-alert error" id="settings-error">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
              {error}
            </div>
          )}

          {success && (
            <div className="settings-alert success" id="settings-success">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit} className="settings-form" id="settings-form">
            <div className="settings-field">
              <label htmlFor="settings-username">Username</label>
              <div className="settings-input-wrapper">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <input
                  id="settings-username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Username"
                />
              </div>
            </div>

            <div className="settings-divider">
              <span>Change Password</span>
            </div>

            <div className="settings-field">
              <label htmlFor="settings-password">New Password</label>
              <div className="settings-input-wrapper">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                <input
                  id="settings-password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Leave blank to keep current"
                />
              </div>
            </div>

            <div className="settings-field">
              <label htmlFor="settings-confirm">Confirm Password</label>
              <div className="settings-input-wrapper">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                <input
                  id="settings-confirm"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Confirm new password"
                />
              </div>
            </div>

            <button
              type="submit"
              className="settings-submit"
              disabled={loading}
              id="settings-submit"
            >
              {loading ? (
                <><div className="spinner" /> Saving...</>
              ) : (
                'Save Changes'
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
