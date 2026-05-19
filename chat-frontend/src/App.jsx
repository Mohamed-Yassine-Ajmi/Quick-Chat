import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Chat from './pages/Chat';
import Settings from './pages/Settings';

function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  const user = localStorage.getItem('user');
  if (!token || !user) return <Navigate to="/login" replace />;
  return children;
}

function PublicRoute({ children }) {
  const token = localStorage.getItem('token');
  const user = localStorage.getItem('user');
  if (token && user) return <Navigate to="/chat" replace />;
  return children;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
        <Route path="/chat" element={<ProtectedRoute><Chat /></ProtectedRoute>} />
        <Route path="/settings" element={<ProtectedRoute><Settings /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/chat" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
