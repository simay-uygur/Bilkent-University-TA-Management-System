// src/components/InstructorLayout.tsx
import React from 'react';
import { Outlet } from 'react-router-dom';
import DeanOfNavBar from '../NavBars/DeanOfNavBar';

const InstructorLayout: React.FC = () => (
  <>
    <DeanOfNavBar />
    <Outlet />
  </>
);

export default InstructorLayout;
