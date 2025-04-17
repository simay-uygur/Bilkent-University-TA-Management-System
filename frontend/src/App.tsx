// src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './components/LoginPage';
import AdminDashboard from './components/AdminDashboard.tsx';
import InstructorDashboard from './components/InstructorDashboard.tsx';
import TADashboard from './components/TADashboard.tsx';

const App: React.FC = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/admin" element={<AdminDashboard />} />
      <Route path="/instructor" element={<InstructorDashboard />} />
      <Route path="/dashboard" element={<TADashboard />} />
      {/* add a catch‑all 404 if you like */}
    </Routes>
  </BrowserRouter>
);

export default App;
