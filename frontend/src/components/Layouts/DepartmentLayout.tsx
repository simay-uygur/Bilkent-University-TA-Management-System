// src/components/InstructorLayout.tsx
import React from 'react';
import { Outlet } from 'react-router-dom';
import DepOfNavBar from '../NavBars/DepOfNavBar';

const InstructorLayout: React.FC = () => (
  <>
    <DepOfNavBar />
    <Outlet />
  </>
);

export default InstructorLayout;
