// src/components/InstructorLayout.tsx
import React from 'react';
import { Outlet } from 'react-router-dom';
import InsNavBar from '../NavBars/InsNavBar';

const InstructorLayout: React.FC = () => (
  <>
    <InsNavBar />
    <Outlet />
  </>
);

export default InstructorLayout;
