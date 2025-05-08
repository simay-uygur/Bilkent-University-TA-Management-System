// src/components/InstructorLayout.tsx
import React from 'react';
import { Outlet } from 'react-router-dom';
import AdminNavBar from '../NavBars/AdminNavBar';

const AdminLayout: React.FC = () => (
  <>
    <AdminNavBar />
    <Outlet />
  </>
);

export default AdminLayout;
