// src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './components/LoginPage';
import AdminDashboard from './components/AdminDashboard';
import InstructorDashboard from './components/InstructorDashboard';
import TADashboard from './components/TADashboard';
import LeaveRequestForm from './components/LeaveRequestForm';
import VolunteerProctoring from './components/VolunteerProctoring';
import Layout from './components/Layout';

const App: React.FC = () => (
  <BrowserRouter>
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/admin" element={<AdminDashboard />} />
      <Route path="/instructor" element={<InstructorDashboard />} />
      
      {/* Routes with the TA nav bar */}
      <Route element={<Layout />}>
        <Route path="/dashboard" element={<TADashboard />} />
        <Route path="/volunteer" element={<VolunteerProctoring />} />
        <Route path="/leave-request/:scheduleId" element={<LeaveRequestForm />} />
        <Route path="/notifications" element={<Navigate to="/dashboard" />} />
        {/* you can add instructor/admin protected routes here */}
      </Route>

      {/* Fallback: redirect all unknown paths to login or dashboard */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  </BrowserRouter>
);

export default App;
