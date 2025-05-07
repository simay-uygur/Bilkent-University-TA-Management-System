import React from 'react';
import { Outlet } from 'react-router-dom';
import TANavBar from '../NavBars/TANavBar.tsx';

const Layout: React.FC = () => {
  return (
    <>
      <TANavBar/>
      <Outlet />
    </>
  );
};

export default Layout;
